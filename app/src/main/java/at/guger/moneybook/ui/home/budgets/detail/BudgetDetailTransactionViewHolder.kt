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

package at.guger.moneybook.ui.home.budgets.detail

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.viewholder.ModelViewHolder
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.ItemTransactionBinding

/**
 * [RecyclerView.ViewHolder] for a transaction item.
 */
class BudgetDetailTransactionViewHolder(binding: ItemTransactionBinding, private val viewModel: BudgetDetailViewModel) :
    ModelViewHolder<ItemTransactionBinding, Transaction>(binding),
    View.OnClickListener, View.OnLongClickListener {

    override fun bind(model: Transaction) {
        binding.transaction = model
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        binding.executePendingBindings()
    }

    override fun onClick(p0: View?) {
        viewModel.onItemClick(absoluteAdapterPosition)
    }

    override fun onLongClick(p0: View?): Boolean {
        viewModel.onLongClick(absoluteAdapterPosition)

        return true
    }
}