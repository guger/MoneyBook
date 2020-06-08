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

package at.guger.moneybook.util.menu

import android.view.Menu
import android.view.MenuItem
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.adapter.CheckableListAdapter
import at.guger.moneybook.data.model.Transaction

/**
 * Util class for [transactions][Transaction] menu.
 */
object TransactionMenuUtils {

    fun prepareMenu(menu: Menu, adapter: CheckableListAdapter<Transaction, *>, markAsPaid: Boolean = false) {
        with(adapter) {
            menu.findItem(R.id.actionTransactionEdit).isVisible = checkedCount == 1
            menu.findItem(R.id.actionTransactionMarkAsPaid).isVisible = markAsPaid && checkedCount == 1 && !currentList[checkedItems.first()].isPaid
        }
    }

    fun onItemSelected(
        item: MenuItem,
        adapter: CheckableListAdapter<Transaction, *>,
        editAction: (Transaction) -> Unit,
        markAsPaidAction: ((Array<Transaction>) -> Unit)? = null,
        deleteAction: (Array<Transaction>) -> Unit
    ): Boolean {
        return when (item.itemId) {
            R.id.actionTransactionEdit -> {
                editAction(adapter.currentList[adapter.checkedItems.first()])
                true
            }
            R.id.actionTransactionMarkAsPaid -> {
                markAsPaidAction!!.invoke(adapter.currentList.filterIndexed { index, _ -> adapter.checkedItems.contains(index) }.toTypedArray())
                true
            }
            R.id.actionTransactionDelete -> {
                deleteAction(adapter.currentList.filterIndexed { index, _ -> adapter.checkedItems.contains(index) }.toTypedArray())
                true
            }
            else -> false
        }
    }
}