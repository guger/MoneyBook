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

package at.guger.moneybook.util.menu

import android.view.Menu
import android.view.MenuItem
import at.guger.moneybook.R
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.ui.home.accounts.AccountsAdapter

/**
 * Util class for [transactions][Transaction] menu.
 */
object AccountMenuUtils {

    fun prepareMenu(menu: Menu, adapter: AccountsAdapter) {
        with(adapter) {
            menu.findItem(R.id.actionAccountEdit).isVisible = selectedCount == 1
            menu.findItem(R.id.actionAccountDelete).isVisible = adapter.currentList.filterIndexed { index, _ -> selectedItems.contains(index) }.none { it.id == Account.DEFAULT_ACCOUNT_ID }
        }
    }

    fun onItemSelected(item: MenuItem, adapter: AccountsAdapter, editAction: (Account) -> Unit, deleteAction: (Array<Account>) -> Unit): Boolean {
        return when (item.itemId) {
            R.id.actionAccountEdit -> {
                editAction(adapter.currentList[adapter.selectedItems.first()].account)
                true
            }
            R.id.actionAccountDelete -> {
                deleteAction(adapter.currentList.filterIndexed { index, _ -> adapter.selectedItems.contains(index) }.map { it.account }.toTypedArray())
                true
            }
            else -> false
        }
    }
}