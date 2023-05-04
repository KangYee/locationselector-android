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

package com.kangyee.locationselector.locationselector.common.state

import com.kangyee.locationselector.locationselector.common.constant.LocationSelectorConstants

data class ListDataUiState<T>(

    /**
     * 操作是否成功
     */
    var isSuccess: Boolean = true,

    /**
     * 当前请求是否为刷新操作
     */
    val isRefresh: Boolean = true,

    /**
     * 接口返回的数据是否为空
     */
    val isEmpty: Boolean = false,

    /**
     * 接口返回的数据是否大于等于 LocationSelectorConstants.PAGE_MAX_COUNT 条
     * 若大于等于则此值为 true，标识将会触发分页加载
     */
    val hasMore: Boolean = false,

    /**
     * 是第一页且没有数据
     */
    val isFirstEmpty: Boolean = false,

    /**
     * 接口返回的列表数据
     */
    val dataList: List<T> = arrayListOf()

) {

    constructor(isRefresh: Boolean = true, dataList: List<T> = arrayListOf()) : this(
        isRefresh = isRefresh,
        isEmpty = dataList.isEmpty(),
        hasMore = dataList.size >= LocationSelectorConstants.PAGE_MAX_COUNT,
        isFirstEmpty = isRefresh && dataList.isEmpty(),
        dataList = dataList
    )

    constructor(isSuccess: Boolean = false, isRefresh: Boolean = true, dataList: List<T> = arrayListOf()) : this(
        isSuccess = isSuccess,
        isRefresh = isRefresh,
        isEmpty = dataList.isEmpty(),
        hasMore = dataList.size >= LocationSelectorConstants.PAGE_MAX_COUNT,
        isFirstEmpty = isRefresh && dataList.isEmpty(),
        dataList = dataList
    )

}
