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

package at.guger.moneybook.ui.home.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.ui.recyclerview.adapter.CheckableListAdapter
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.databinding.ItemAccountBinding
import at.guger.moneybook.ui.home.HomeViewModel

/**
 * [RecyclerView.Adapter] showing all accounts and details.
 */
class AccountsAdapter(private val viewModel: HomeViewModel) : CheckableListAdapter<AccountWithBalance, AccountViewHolder>(AccountsDiffCallback()) {

    //region Variables

    override val checkedItems: MutableList<Int> = mutableListOf()

    //endregion

    //region Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        return AccountViewHolder(ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false), viewModel)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
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
        checkedItems.forEach(::notifyItemChanged)

        super.clearChecked()
    }

    //endregion

    class AccountsDiffCallback : DiffUtil.ItemCallback<AccountWithBalance>() {
        override fun areItemsTheSame(oldItem: AccountWithBalance, newItem: AccountWithBalance): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AccountWithBalance, newItem: AccountWithBalance): Boolean {
            return oldItem == newItem
        }
    }
}