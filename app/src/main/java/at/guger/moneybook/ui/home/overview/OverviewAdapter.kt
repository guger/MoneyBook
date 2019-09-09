/*
 * Copyright 2019 Daniel Guger
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

package at.guger.moneybook.ui.home.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.home.overview.accounts.OverviewAccountsViewHolder

/**
 * [RecyclerView.Adapter] showing an overview of all account balances.
 */
class OverviewAdapter(private val viewModel: HomeViewModel) : RecyclerView.Adapter<BindingViewHolder<*, HomeViewModel>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<*, HomeViewModel> {
        return when (viewType) {
            OVERVIEW_ACCOUNTS -> OverviewAccountsViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_overview_accounts,
                    parent,
                    false
                )
            )
            OVERVIEW_DUES -> TODO()
            OVERVIEW_PLANNING -> TODO()
            else -> throw IllegalArgumentException("This type of view holder does not exist.")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder<*, HomeViewModel>, position: Int) {
        holder.bind(viewModel)
    }

    override fun getItemCount(): Int = ITEM_COUNT

    companion object {
        const val OVERVIEW_ACCOUNTS = 0
        const val OVERVIEW_DUES = 1
        const val OVERVIEW_PLANNING = 2

        const val ITEM_COUNT = 1//todo 3
    }
}