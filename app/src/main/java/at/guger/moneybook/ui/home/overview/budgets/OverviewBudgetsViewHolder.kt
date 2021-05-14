/*
 * Copyright 2021 Daniel Guger
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

package at.guger.moneybook.ui.home.overview.budgets

import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.databinding.ItemOverviewBudgetsBinding
import at.guger.moneybook.ui.home.HomeViewModel
import kotlin.math.min

/**
 * [RecyclerView.ViewHolder] for the budgets overview item.
 */
class OverviewBudgetsViewHolder(binding: ItemOverviewBudgetsBinding) : BindingViewHolder<ItemOverviewBudgetsBinding, HomeViewModel>(binding) {

    override fun bind(viewModel: HomeViewModel) {
        binding.viewModel = viewModel
        binding.executePendingBindings()

        viewModel.budgetsWithBalance.observe(binding.lifecycleOwner!!, { budgets ->
            val leftValue: Float = budgets.sumOf { it.budget - it.balance }.toFloat()

            val distributions = mutableListOf(*budgets.map { it.balance.toFloat() }.toTypedArray(), leftValue)

            val colors = mutableListOf(*budgets.map { it.color }.toTypedArray(), Color.BLACK)

            binding.mOverviewBudgetsDivider.setDistributions(
                distributions = distributions,
                colors = colors
            )
        })

        binding.mOverviewBudgetsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = OverviewBudgetsListAdapter().apply {
                viewModel.budgetsWithBalance.observe(binding.lifecycleOwner!!, { budgets ->
                    submitList(budgets.sortedBy { if (it.balance > 0) it.budget - it.balance else Double.MAX_VALUE }.subList(0, min(budgets.size, 3)))
                })
            }
        }
    }
}