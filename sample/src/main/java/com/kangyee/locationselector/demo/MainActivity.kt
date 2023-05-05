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

package com.kangyee.locationselector.demo

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kangyee.locationselector.demo.util.PermissionUtils
import com.kangyee.locationselector.locationselector.common.constant.LocationSelectorConstants
import com.kangyee.locationselector.locationselector.model.LocationSelectorResultModel
import com.kangyee.locationselector.locationselector.ui.fragment.LocationSelectorFragment
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 请注意确保在用户知情且同意的情况下，调用以下两个方法，规避合规问题
        TencentMapInitializer.setAgreePrivacy(true)
        TencentLocationManager.setUserAgreePrivacy(true)

        // 务必确保请求了权限
        PermissionUtils.requestPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION) {

            val transaction = supportFragmentManager.beginTransaction()
            supportFragmentManager.setFragmentResultListener(
                LocationSelectorConstants.REQUEST_KEY, this
            ) { requestKey, bundle ->
                val result = bundle.getString(LocationSelectorConstants.RESULT_KEY)
                Log.i("LocationSelectorResult", result ?: "NULL")
            }
            transaction.replace(R.id.fl_map_container, LocationSelectorFragment())
            transaction.commitNow()

        }
    }

}