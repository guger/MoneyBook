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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.R
import kotlinx.android.synthetic.main.item_scrollable_tab.view.*

/**
 * TODO
 */
class TabAdapter(
    private val tabs: MutableList<String> = mutableListOf(),
    @StyleRes
    private val tabTextStyle: Int
) : RecyclerView.Adapter<TabAdapter.TabViewHolder>() {

    private var listener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scrollable_tab, parent, false)

        TextViewCompat.setTextAppearance(view.txvScrollableTabTitle, tabTextStyle)
        return TabViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun onBindViewHolder(
        holder: TabViewHolder,
        position: Int
    ) {
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

    internal fun listenTabChangeForPager() {}

    class TabViewHolder(
        view: View,
        private val listener: ((position: Int) -> Unit)?
    ) : RecyclerView.ViewHolder(view) {

        private val tab: TextView = view.txvScrollableTabTitle

        init {
            view.setOnClickListener {
                listener?.invoke(adapterPosition)
            }
        }

        fun bind(model: String) {
            tab.text = model
        }
    }
}
