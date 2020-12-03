/*
 * Copyright 2020 Daniel Guger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.guger.moneybook.core.ui.widget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.databinding.ItemScrollableTabBinding

/**
 * Adapter for tabs of [ScrollableTab].
 */
class TabAdapter(private val tabs: MutableList<String> = mutableListOf(), @StyleRes private val tabTextStyle: Int) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    //region Variables

    private lateinit var binding: ItemScrollableTabBinding

    private var listener: ((position: Int) -> Unit)? = null

    //endregion

    //region Methods

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        binding = ItemScrollableTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        TextViewCompat.setTextAppearance(binding.txvScrollableTabTitle, tabTextStyle)

        return TabViewHolder(binding, listener)
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        holder.bind(tabs[position])
    }

    fun addAll(list: List<String>) {
        tabs.clear()
        tabs.addAll(list)
        notifyDataSetChanged()
    }

    fun onTabClick(listener: ((position: Int) -> Unit)?) {
        this.listener = listener
    }

    //endregion

    class TabViewHolder(private val binding: ItemScrollableTabBinding, private val listener: ((position: Int) -> Unit)?) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                listener?.invoke(adapterPosition)
            }
        }

        fun bind(model: String) {
            binding.txvScrollableTabTitle.text = model
        }
    }
}
