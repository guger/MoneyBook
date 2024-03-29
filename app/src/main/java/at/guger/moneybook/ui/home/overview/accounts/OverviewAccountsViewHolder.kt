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

package at.guger.moneybook.ui.home.overview.accounts

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.databinding.ItemOverviewAccountsBinding
import at.guger.moneybook.ui.home.HomeViewModel
import kotlin.math.min

/**
 * [RecyclerView.ViewHolder] for the accounts overview item.
 */
class OverviewAccountsViewHolder(binding: ItemOverviewAccountsBinding) : BindingViewHolder<ItemOverviewAccountsBinding, HomeViewModel>(binding) {

    override fun bind(viewModel: HomeViewModel) {
        binding.viewModel = viewModel
        binding.executePendingBindings()

        viewModel.accounts.observe(binding.lifecycleOwner!!) { coloredAccounts ->
            binding.mOverviewAccountsDivider.setDistributions(
                distributions = coloredAccounts.map { it.balance.toFloat() },
                colors = coloredAccounts.map { it.color }
            )
        }

        binding.mOverviewAccountsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = OverviewAccountsListAdapter(viewModel).apply {
                viewModel.accounts.observe(binding.lifecycleOwner!!) { accounts ->
                    submitList(accounts.subList(0, min(accounts.size, 4)))
                }
            }
        }
    }
}