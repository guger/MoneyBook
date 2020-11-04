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

package at.guger.moneybook.ui.home.overview.accounts

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.databinding.ItemOverviewAccountsBinding
import at.guger.moneybook.ui.home.HomeViewModel
import kotlinx.android.synthetic.main.item_overview_accounts.*

/**
 * [RecyclerView.ViewHolder] for the accounts overview item.
 */
class OverviewAccountsViewHolder(binding: ItemOverviewAccountsBinding) : BindingViewHolder<ItemOverviewAccountsBinding, HomeViewModel>(binding) {

    override fun bind(viewModel: HomeViewModel) {
        binding.viewModel = viewModel
        binding.executePendingBindings()

        viewModel.coloredAccounts.observe(binding.lifecycleOwner!!, { coloredAccounts ->
            mOverviewAccountsDivider.setDistributions(
                distributions = coloredAccounts.map { (it.balance + it.startBalance).toFloat() },
                colors = coloredAccounts.map { it.color }
            )
        })

        with(mOverviewAccountsRecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = OverviewAccountsListAdapter(viewModel).apply { viewModel.coloredAccounts.observe(binding.lifecycleOwner!!, Observer(::submitList)) }
        }
    }

    override fun clear() {}
}