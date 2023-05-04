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

package com.kangyee.locationselector.locationselector.ui.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.kangyee.locationselector.locationselector.R
import com.kangyee.locationselector.locationselector.databinding.ItemLocationSelectorAddressBinding
import com.kangyee.locationselector.locationselector.model.TencentPoiModel

/**
 * @author KangYee
 * @description
 **/
class LocationSelectorAddressAdapter : BaseQuickAdapter<TencentPoiModel, BaseViewHolder>(
    R.layout.item_location_selector_address
), LoadMoreModule {

    var activePosition = -1

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        // 绑定 view
        DataBindingUtil.bind<ViewDataBinding>(viewHolder.itemView)
    }

    override fun convert(holder: BaseViewHolder, item: TencentPoiModel) {
        val binding: ItemLocationSelectorAddressBinding? = DataBindingUtil.getBinding(holder.itemView)
        binding?.let {
            it.model = item
            it.isCheck = holder.absoluteAdapterPosition == activePosition

            it.executePendingBindings()
        }
    }

}