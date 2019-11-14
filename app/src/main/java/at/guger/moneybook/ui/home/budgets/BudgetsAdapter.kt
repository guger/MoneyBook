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

package at.guger.moneybook.ui.home.budgets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.adapter.CheckableList
import at.guger.moneybook.data.model.BudgetWithBalance

/**
 * [RecyclerView.Adapter] showing all budgets.
 */
class BudgetsAdapter : ListAdapter<BudgetWithBalance, BudgetViewHolder>(BudgetsDiffCallback()), CheckableList {

    //region Variables

    override val checkedItems: MutableList<Int> = mutableListOf()

    //endregion

    //region Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        return BudgetViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_budget, parent, false))
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))

        holder.itemView.isActivated = checkedItems.contains(position)
    }

    //endregion

    //region Methods

    override fun toggleChecked(pos: Int) {
        super.toggleChecked(pos)

        notifyItemChanged(pos)
    }

    override fun clearChecked() {
        super.clearChecked()

        notifyDataSetChanged()
    }

    //endregion

    class BudgetsDiffCallback : DiffUtil.ItemCallback<BudgetWithBalance>() {

        override fun areItemsTheSame(oldItem: BudgetWithBalance, newItem: BudgetWithBalance): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BudgetWithBalance, newItem: BudgetWithBalance): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name && oldItem.balance == newItem.balance && oldItem.budget == newItem.budget && oldItem.color == newItem.color
        }
    }
}