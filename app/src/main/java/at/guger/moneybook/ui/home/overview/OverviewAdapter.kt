/*
 * Copyright 2022 Daniel Guger
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
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.databinding.ItemOverviewAccountsBinding
import at.guger.moneybook.databinding.ItemOverviewBudgetsBinding
import at.guger.moneybook.databinding.ItemOverviewDuesBinding
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.home.overview.accounts.OverviewAccountsViewHolder
import at.guger.moneybook.ui.home.overview.budgets.OverviewBudgetsViewHolder
import at.guger.moneybook.ui.home.overview.dues.OverviewDuesViewHolder

/**
 * [RecyclerView.Adapter] showing an overview of account balances, dues and budgets.
 */
class OverviewAdapter(private val viewModel: HomeViewModel, private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<BindingViewHolder<*, HomeViewModel>>() {

    //region Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<*, HomeViewModel> {
        return when (viewType) {
            OVERVIEW_ACCOUNTS -> {
                val binding = ItemOverviewAccountsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.lifecycleOwner = lifecycleOwner

                OverviewAccountsViewHolder(binding)
            }
            OVERVIEW_DUES -> {
                val binding = ItemOverviewDuesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.lifecycleOwner = lifecycleOwner

                OverviewDuesViewHolder(binding)
            }
            OVERVIEW_BUDGETS -> {
                val binding = ItemOverviewBudgetsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                binding.lifecycleOwner = lifecycleOwner

                OverviewBudgetsViewHolder(binding)
            }
            else -> throw IllegalArgumentException("This type of view holder does not exist.")
        }
    }

    override fun onBindViewHolder(holder: BindingViewHolder<*, HomeViewModel>, position: Int) {
        holder.bind(viewModel)
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemCount(): Int = ITEM_COUNT

    //endregion

    companion object {
        const val OVERVIEW_ACCOUNTS = 0
        const val OVERVIEW_DUES = 1
        const val OVERVIEW_BUDGETS = 2

        const val ITEM_COUNT = 3
    }
}