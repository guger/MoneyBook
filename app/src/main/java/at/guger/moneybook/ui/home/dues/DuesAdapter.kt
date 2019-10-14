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

package at.guger.moneybook.ui.home.dues

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.adapter.CheckableList
import at.guger.moneybook.data.model.Transaction

/**
 * [RecyclerView.Adapter] showing all accounts and details.
 */
class DuesAdapter : ListAdapter<Transaction, DuesTransactionViewHolder>(TransactionsDiffCallback()), CheckableList {

    //region Variables

    override val checkedItems: MutableList<Int> = mutableListOf()

    //endregion

    //region Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DuesTransactionViewHolder {
        return DuesTransactionViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_transaction, parent, false))
    }

    override fun onBindViewHolder(holder: DuesTransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
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

    class TransactionsDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}