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

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kangyee.locationselector.locationselector.common.state.ListDataUiState
import com.kangyee.locationselector.locationselector.model.TencentPoiModel
import com.tencent.lbssearch.TencentSearch
import com.tencent.lbssearch.httpresponse.BaseObject
import com.tencent.lbssearch.httpresponse.HttpResponseListener
import com.tencent.lbssearch.`object`.param.Geo2AddressParam
import com.tencent.lbssearch.`object`.param.SearchParam
import com.tencent.lbssearch.`object`.result.Geo2AddressResultObject
import com.tencent.lbssearch.`object`.result.SearchResultObject
import com.tencent.tencentmap.mapsdk.maps.model.LatLng


class LocationSelectorViewModel : ViewModel() {

    val keyword = ObservableField<String>()

    val data = MutableLiveData<ListDataUiState<TencentPoiModel>>()

    var mCurrentLatLng: LatLng? = null

    private var page = 1

    fun requestData(tencentSearch: TencentSearch, isRefresh: Boolean = true) {
        if (mCurrentLatLng == null) {
            return
        }
        val localKeyword = keyword.get()
        if (localKeyword.isNullOrBlank()) {
            geo2Address(tencentSearch, isRefresh)
        } else {
            searchPoi(tencentSearch, isRefresh)
        }
    }

    private fun searchPoi(tencentSearch: TencentSearch, isRefresh: Boolean = true) {
        if (mCurrentLatLng == null) {
            return
        }
        if (isRefresh) {
            page = 1
        }

        val region = SearchParam.Nearby(mCurrentLatLng, 4000).autoExtend(false)
        val searchParam = SearchParam(keyword.get(), region)
        searchParam.pageIndex(page)

        tencentSearch.search(searchParam, object : HttpResponseListener<BaseObject> {
            override fun onSuccess(p0: Int, p1: BaseObject?) {
                runCatching {
                    val gson = Gson()
                    val resultObject = p1 as SearchResultObject
                    val poiJson = gson.toJson(resultObject.data)
                    val typeToken = object : TypeToken<List<TencentPoiModel>>() {}
                    val newPoiList = gson.fromJson<List<TencentPoiModel>>(poiJson, typeToken.type)
                    val listDataUiState = ListDataUiState(
                        isRefresh = isRefresh,
                        dataList = newPoiList
                    )

                    listDataUiState
                }.onSuccess {
                    page++
                    data.value = it
                }.onFailure {
                    processGetDataListException(isRefresh)
                }
            }

            override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                processGetDataListException(isRefresh)
                Log.e("tencentSearch.search", "code: $p0, message: $p1")
            }
        })
    }

    private fun geo2Address(tencentSearch: TencentSearch, isRefresh: Boolean = true) {
        if (mCurrentLatLng == null) {
            return
        }
        if (isRefresh) {
            page = 1
        }

        val geo2AddressParam = Geo2AddressParam(mCurrentLatLng).getPoi(true)
            .setPoiOptions(
                Geo2AddressParam.PoiOptions()
                    .setRadius(4000)
                    .setPageIndex(page)
            )
        tencentSearch.geo2address(geo2AddressParam, object : HttpResponseListener<BaseObject> {
            override fun onSuccess(p0: Int, p1: BaseObject?) {
                runCatching {
                    val gson = Gson()
                    val resultObject = p1 as Geo2AddressResultObject
                    val poiJson = gson.toJson(resultObject.result.pois)
                    val typeToken = object : TypeToken<List<TencentPoiModel>>() {}
                    val newPoiList = gson.fromJson<List<TencentPoiModel>>(poiJson, typeToken.type)
                    val listState = ListDataUiState(
                        isRefresh = isRefresh,
                        dataList = newPoiList
                    )

                    listState
                }.onSuccess {
                    page++
                    data.value = it
                }.onFailure {
                    processGetDataListException(isRefresh)
                }
            }

            override fun onFailure(p0: Int, p1: String?, p2: Throwable?) {
                processGetDataListException(isRefresh)
                Log.e("tencentSearch.geo2address", "code: $p0, message: $p1")
            }
        })
    }

    private fun processGetDataListException(isRefresh: Boolean = true) {
        val listState = ListDataUiState(
            isSuccess = false,
            isRefresh = isRefresh,
            dataList = arrayListOf<TencentPoiModel>()
        )
        data.value = listState
    }

}