/*
 * Copyright (C) 2023 KangYee
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kangyee.locationselector.locationselector.ui.fragment

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.kangyee.locationselector.locationselector.R
import com.kangyee.locationselector.locationselector.base.BaseFragment
import com.kangyee.locationselector.locationselector.common.constant.LocationSelectorConstants
import com.kangyee.locationselector.locationselector.common.ext.dp
import com.kangyee.locationselector.locationselector.common.ext.dpInt
import com.kangyee.locationselector.locationselector.databinding.FragmentLocationSelectorBinding
import com.kangyee.locationselector.locationselector.model.LocationSelectorResultModel
import com.kangyee.locationselector.locationselector.ui.adapter.LocationSelectorAddressAdapter
import com.tencent.lbssearch.TencentSearch
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.tencentmap.mapsdk.maps.*
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle
import kotlin.math.roundToInt

class LocationSelectorFragment : BaseFragment<LocationSelectorViewModel, FragmentLocationSelectorBinding>(),
    LocationSource, TencentLocationListener, TencentMap.OnCameraChangeListener {

    companion object {

        @JvmStatic
        fun newInstance(): LocationSelectorFragment {
            return LocationSelectorFragment()
        }

    }

    /**
     * 腾讯地图相关
     */
    private var mMapView: MapView? = null
    private var mTencentMap: TencentMap? = null
    private var mLocationManager: TencentLocationManager? = null
    private var mLocationRequest: TencentLocationRequest? = null
    private var mLocationChangedListener: LocationSource.OnLocationChangedListener? = null
    private var mLocationStyle: MyLocationStyle? = null

    /**
     * 搜索相关
     */
    private val mTencentSearch by lazy { TencentSearch(context) }

    /**
     * 是否为首次定位
     * 如果是首次定位需要进行相机切换
     */
    private var mIsFirstLocate = true

    /**
     * BottomSheet 相关变量
     */
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var mIvCollapseHeight = 0
    private var mBottomSheetHeight = 0

    /**
     * 列表Adapter
     */
    private val mAdapter by lazy { LocationSelectorAddressAdapter() }

    /**
     * 用于数据回传的位置信息
     */
    private var mSelectAddress: String? = null
    private var mSelectLng: String? = null
    private var mSelectLat: String? = null

    /**
     * 存储上一次触发 onCameraChange 的时间戳，用于简单的流控
     */
    private var mLastCameraChangeTimestamp = 0L

    override fun layoutId() = R.layout.fragment_location_selector

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.proxyClick = ProxyClick()
        mBinding.viewModel = mViewModel

        initBottomSheet()
        initMap()

        mBinding.etKeyword.addTextChangedListener(mTextWatcher)

        mBinding.root.post {
            val bottomSheetLayoutParams = mBinding.viewBottomSheet.layoutParams
            bottomSheetLayoutParams.height = (ScreenUtils.getScreenHeight() * 0.7f).toInt()
            mBinding.viewBottomSheet.requestLayout()

            mIvCollapseHeight = mBinding.ivCollapse.measuredHeight

            val ivCollapseParams = mBinding.ivCollapse.layoutParams
            ivCollapseParams.height = 0
            mBinding.ivCollapse.requestLayout()

            val rect = Rect()
            mActivity.window.decorView.getWindowVisibleDisplayFrame(rect)
            mBinding.vTitleBar.layoutParams.height = rect.top
            mBinding.vTitleBar.requestLayout()
        }

        initAdapter()

        KeyboardUtils.registerSoftInputChangedListener(mActivity.window) { height ->
            if (height > 0) {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        playIvCenterPosAnim()
        processLayoutParams(0F)
    }

    override fun createObserver() {
        mViewModel.data.observe(this) { state ->
            if (state.isSuccess) {
                // 是否成功
                when {
                    state.isFirstEmpty -> {
                        // 第一页且无数据
                        mAdapter.loadMoreModule.loadMoreEnd(true)
                    }
                    state.isRefresh -> {
                        // 第一页
                        mAdapter.setList(state.dataList)
                        if (state.dataList.size < LocationSelectorConstants.PAGE_MAX_COUNT) {
                            // 返回的数据小于 LocationSelectorConstants.PAGE_MAX_COUNT 条
                            mAdapter.loadMoreModule.loadMoreEnd(true)
                        }
                    }
                    else -> {
                        // 不是第一页
                        mAdapter.addData(state.dataList)
                        if (state.dataList.isEmpty()) {
                            // 数据是否为空
                            mAdapter.loadMoreModule.loadMoreEnd(true)
                        }
                    }
                }
                return@observe
            }
            // 失败的情况下只需判断是否为第一页
            if (!state.isRefresh) {
                mAdapter.loadMoreModule.loadMoreFail()
            }
        }
    }

    private fun initBottomSheet() {
        // BottomSheet 相关
        mBottomSheetBehavior = BottomSheetBehavior.from(mBinding.viewBottomSheet)
        mBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        KeyboardUtils.hideSoftInput(bottomSheet)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        KeyboardUtils.hideSoftInput(bottomSheet)
                    }
                    else -> {

                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                mBottomSheetHeight = bottomSheet.height
                processLayoutParams(slideOffset)
            }
        })

    }

    private fun initMap() {
        initLocation()
        setLocMarkerStyle()

        // 地图相关
        mMapView = TextureMapView(context)
        mBinding.flMapContainer.addView(mMapView)

        mMapView?.let { mapView ->
            // 获取地图实例
            mTencentMap = mapView.map

            mTencentMap?.let { tencentMap ->
                tencentMap.setLocationSource(this)
                tencentMap.isMyLocationEnabled = true

                tencentMap.setOnCameraChangeListener(this)

                tencentMap.setMyLocationStyle(mLocationStyle)

                tencentMap.uiSettings.setLogoPosition(
                    TencentMapOptions.LOGO_POSITION_BOTTOM_RIGHT,
                    intArrayOf(16F.dpInt, 8F.dpInt)
                )
                tencentMap.uiSettings.isTiltGesturesEnabled = false
                tencentMap.uiSettings.isRotateGesturesEnabled = false
            }
        }
    }

    private fun processLayoutParams(slideOffset: Float) {
        val interpolator = LinearInterpolator()
        val marginBottom = 8F.dpInt
        val bottomHeight = mBottomSheetHeight - mBottomSheetBehavior.peekHeight
        // 计算插值后的偏移量
        val offset = interpolator.getInterpolation(slideOffset) * bottomHeight
        setClMapAreaMargin(offset.roundToInt() + marginBottom)

        val ivCollapseParams = mBinding.ivCollapse.layoutParams
        ivCollapseParams.height = (mIvCollapseHeight * slideOffset).toInt()
        mBinding.ivCollapse.requestLayout()
    }

    private fun setClMapAreaMargin(value: Int) {
        val params = mBinding.clMapArea.layoutParams as CoordinatorLayout.LayoutParams
        params.bottomMargin = value

        mBinding.clMapArea.requestLayout()
    }

    private fun playIvCenterPosAnim() {
        val animation = ObjectAnimator.ofFloat(
            mBinding.ivCenterPos,
            "translationY",
            0F, (-18F).dp, 0F
        )
        animation.duration = 1000
        animation.interpolator = BounceInterpolator()
        animation.target = mBinding.ivCenterPos
        animation.start()
    }

    private fun initAdapter() {
        mBinding.rvAddressList.layoutManager = LinearLayoutManager(context)
        mBinding.rvAddressList.adapter = mAdapter

        mAdapter.setOnItemClickListener { _, _, position ->
            val data = mAdapter.getItem(position)
            if (mSelectAddress == data.address) {
                return@setOnItemClickListener
            }

            mLastCameraChangeTimestamp = System.currentTimeMillis()

            val lng = data.latLng?.longitude ?: 0.0
            val lat = data.latLng?.latitude ?: 0.0
            mSelectAddress = data.address.toString()
            mSelectLng = lng.toString()
            mSelectLat = lat.toString()

            val latLng = LatLng(lat, lng)
            val cameraPosition = CameraPosition(
                latLng, 15F, 0F, 0F
            )
            val cameraSigma = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mTencentMap?.moveCamera(cameraSigma)
            playIvCenterPosAnim()

            mAdapter.setActivePosition(position)
        }
    }

    /**
     * 定位的一些初始化设置
     */
    private fun initLocation() {
        // 用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        mLocationManager = TencentLocationManager.getInstance(context)
        // 创建定位请求
        mLocationRequest = TencentLocationRequest.create()
        // 设置定位周期（位置监听器回调周期）为3s
        mLocationRequest?.interval = 3000
    }

    override fun onCameraChange(cameraPosition: CameraPosition) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - mLastCameraChangeTimestamp < 1000) {
            // 简单的做一个1秒的流控
            return
        }
        mLastCameraChangeTimestamp = currentTimestamp

        val cameraLat = cameraPosition.target.latitude
        val cameraLng = cameraPosition.target.longitude
        mViewModel.mCurrentLatLng?.let {
            if (it.latitude == cameraLat && it.longitude == cameraLng) {
                return
            }
        }
        mViewModel.mCurrentLatLng = LatLng(cameraLat, cameraLng)
        mViewModel.requestData(mTencentSearch, true)
    }

    override fun onCameraChangeFinished(cameraPosition: CameraPosition) { }

    override fun onLocationChanged(tencentLocation: TencentLocation?, i: Int, s: String?) {
        // 其中 locationChangeListener 为 LocationSource.active 返回给用户的位置监听器
        // 用户通过这个监听器就可以设置地图的定位点位置
        if (i == TencentLocation.ERROR_OK && mLocationChangedListener != null) {
            tencentLocation?.let {
                val location = Location(it.provider)
                // 设置经纬度
                location.latitude = it.latitude
                location.longitude = it.longitude
                // 设置精度，这个值会被设置为定位点上表示精度的圆形半径
                location.accuracy = it.accuracy
                // 设置定位标的旋转角度，注意 tencentLocation.getBearing() 只有在 gps 时才有可能获取
                location.bearing = it.bearing
                // 将位置信息返回给地图
                mLocationChangedListener?.onLocationChanged(location)

                if (mIsFirstLocate) {
                    mIsFirstLocate = false
                    mTencentMap?.let { tencentMap ->
                        val latLng = LatLng(it.latitude, it.longitude)
                        val cameraPosition = CameraPosition(
                            latLng, 15F, 0F, 0F
                        )
                        val cameraSigma = CameraUpdateFactory.newCameraPosition(cameraPosition)
                        tencentMap.moveCamera(cameraSigma)

                        mBinding.ivCenterPos.visibility = View.VISIBLE
                        playIvCenterPosAnim()
                    }
                }
            }
        }
    }

    override fun onStatusUpdate(s: String?, i: Int, s1: String?) {
        Log.v("State changed", "$s===$s1")
    }

    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener?) {
        // 这里我们将地图返回的位置监听保存为当前 Activity 的成员变量
        mLocationChangedListener = onLocationChangedListener
        mLocationManager?.let { locationManager ->
            // 开启定位
            val err: Int = locationManager.requestLocationUpdates(
                mLocationRequest, this, Looper.myLooper())
            when (err) {
                1 -> Toast.makeText(context,
                    "设备缺少使用腾讯定位服务需要的基本条件",
                    Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(context,
                    "manifest 中配置的 key 不正确", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(context,
                    "自动加载libtencentloc.so失败", Toast.LENGTH_SHORT).show()
                else -> { }
            }
        }
    }

    override fun deactivate() {
        // 当不需要展示定位点时，需要停止定位并释放相关资源
        mLocationManager?.removeUpdates(this)
        mLocationManager = null
        mLocationRequest = null
        mLocationChangedListener = null
    }

    private val mTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (mAdapter.getActivePosition() != -1) {
                mAdapter.setActivePosition(-1)
            }
            mViewModel.requestData(mTencentSearch, true)
        }
    }

    /**
     * 设置定位图标样式
     */
    private fun setLocMarkerStyle() {
        mLocationStyle = MyLocationStyle()
        mLocationStyle?.let {
            // 创建图标
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                getBitmap(R.drawable.ic_locate_map_current_pos)
            )
            it.icon(bitmapDescriptor)

            it.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
        }
    }

    private fun getBitmap(resourceId: Int): Bitmap? {
        var bitmap = BitmapFactory.decodeResource(resources, resourceId)
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = 55
        val newHeight = 55
        val widthScale = newWidth.toFloat() / width
        val heightScale = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(widthScale, heightScale)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()

        KeyboardUtils.unregisterSoftInputChangedListener(mActivity.window)
        mBinding.etKeyword.removeTextChangedListener(mTextWatcher)
    }

    inner class ProxyClick {

        fun onClickBack() {
            mActivity.onBackPressedDispatcher.onBackPressed()
        }

        fun onClickDone() {
            if (TextUtils.isEmpty(mSelectAddress)) {
                ToastUtils.showShort("请在下方列表中选择具体地点")
                return
            }
            val model = LocationSelectorResultModel(
                mSelectAddress, mSelectLat, mSelectLng
            )
            onClickBack()
        }

        fun onClickCollapse() {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        fun onCleanKeyword() {
            mViewModel.keyword.set("")
            mViewModel.requestData(mTencentSearch)
        }

    }

}