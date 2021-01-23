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

package at.guger.moneybook.ui.home.overview.dues

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.viewholder.BindingViewHolder
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.ItemOverviewDuesBinding
import at.guger.moneybook.ui.home.HomeViewModel
import kotlin.math.min

/**
 * [RecyclerView.ViewHolder] for the dues overview item.
 */
class OverviewDuesViewHolder(binding: ItemOverviewDuesBinding) : BindingViewHolder<ItemOverviewDuesBinding, HomeViewModel>(binding) {

    override fun bind(viewModel: HomeViewModel) {
        binding.viewModel = viewModel
        binding.executePendingBindings()

        viewModel.claimsAndDebts.observe(binding.lifecycleOwner!!, { transactions ->
            binding.mOverviewDuesDivider.setDistributions(
                listOf(
                    transactions.filter { !it.isPaid && it.type == Transaction.TransactionType.CLAIM }.sumByDouble { it.value }.toFloat(),
                    transactions.filter { !it.isPaid && it.type == Transaction.TransactionType.DEBT }.sumByDouble { it.value }.toFloat()
                ),
                colorsRes = listOf(
                    R.color.color_claim,
                    R.color.color_debt
                )
            )
        })

        binding.mOverviewDuesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = OverviewDuesListAdapter().apply {
                viewModel.claimsAndDebts.observe(binding.lifecycleOwner!!, { transactions ->
                    val unpaidDues = transactions.filterNot { it.isPaid }
                    submitList(unpaidDues.subList(0, min(unpaidDues.size, 3)))
                })
            }
        }
    }
}