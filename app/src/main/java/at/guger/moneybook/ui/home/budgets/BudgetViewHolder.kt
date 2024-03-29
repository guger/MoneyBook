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

package at.guger.moneybook.ui.home.budgets

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.viewholder.ModelViewHolder
import at.guger.moneybook.data.model.BudgetWithBalance
import at.guger.moneybook.databinding.ItemBudgetBinding
import at.guger.moneybook.ui.home.HomeViewModel

/**
 * [RecyclerView.ViewHolder] for a budget item.
 */
class BudgetViewHolder(binding: ItemBudgetBinding, private val viewModel: HomeViewModel) : ModelViewHolder<ItemBudgetBinding, BudgetWithBalance>(binding), View.OnClickListener, View.OnLongClickListener {

    override fun bind(model: BudgetWithBalance) {
        binding.budget = model
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        binding.executePendingBindings()
    }

    override fun onClick(v: View?) {
        viewModel.onItemClick(absoluteAdapterPosition)
    }

    override fun onLongClick(v: View?): Boolean {
        viewModel.onLongClick(absoluteAdapterPosition)

        return true
    }
}