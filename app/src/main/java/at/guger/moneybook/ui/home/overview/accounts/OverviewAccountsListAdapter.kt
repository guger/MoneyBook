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

package at.guger.moneybook.ui.home.overview.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.ui.home.ColoredAccount
import at.guger.moneybook.ui.home.HomeViewModel

/**
 * [RecyclerView.Adapter] for overview coloredAccounts card.
 */
class OverviewAccountsListAdapter(private val viewModel: HomeViewModel) : ListAdapter<ColoredAccount, OverviewAccountsAccountViewHolder>(OverviewAccountsDiffCallback()) {

    //region Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewAccountsAccountViewHolder {
        return OverviewAccountsAccountViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_overview_account, parent, false), viewModel)
    }

    override fun onBindViewHolder(holder: OverviewAccountsAccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    //endregion

    class OverviewAccountsDiffCallback : DiffUtil.ItemCallback<ColoredAccount>() {
        override fun areItemsTheSame(oldItem: ColoredAccount, newItem: ColoredAccount): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ColoredAccount, newItem: ColoredAccount): Boolean {
            return oldItem == newItem
        }
    }
}