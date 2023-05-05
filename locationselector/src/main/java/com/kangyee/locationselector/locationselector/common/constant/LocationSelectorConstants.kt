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

package com.kangyee.locationselector.locationselector.common.constant

object LocationSelectorConstants {

    /**
     * 每页的最大数量
     */
    const val PAGE_MAX_COUNT = 10

    /**
     * 用于 setFragmentResultListener 中使用的 requestKey
     */
    const val REQUEST_KEY = "LOCATION_SELECTOR_REQUEST_KEY"

    /**
     * 用于 setFragmentResultListener 中通过 bundle 获取的 key
     * 值类型：不为空的Json字符串
     */
    const val RESULT_KEY = "LOCATION_SELECTOR_RESULT_KEY"

}