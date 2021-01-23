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

package at.guger.moneybook.ui.home.overview

import android.widget.TextView
import androidx.databinding.BindingAdapter
import at.guger.moneybook.core.util.ext.size
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.data.model.BudgetWithBalance
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.util.CurrencyFormat
import kotlin.math.max

/**
 * Binding adapters for the overview screen.
 */

@BindingAdapter("accounts", requireAll = true)
fun TextView.setAccounts(accounts: List<AccountWithBalance>?) {
    accounts?.let { account ->
        setCurrency(account.sumByDouble { it.balance + it.startBalance })
    }
}

@BindingAdapter("dues", requireAll = true)
fun TextView.setDues(transactions: List<Transaction>?) {
    transactions?.let { dues ->
        setCurrency(dues.filterNot { it.isPaid }.sumByDouble { transaction ->
            when (transaction.type) {
                Transaction.TransactionType.CLAIM -> transaction.value * max(transaction.contacts.size(), 1)
                Transaction.TransactionType.DEBT -> -transaction.value * max(transaction.contacts.size(), 1)
                else -> throw IllegalArgumentException("Debt sums must not include earnings or expenses.")
            }
        })
    }
}

@BindingAdapter("budgets")
fun TextView.setBudgets(budgets: List<BudgetWithBalance>?) {
    budgets?.let { budgetsList ->
        setCurrency(budgetsList.sumByDouble { it.budget - it.balance })
    }
}

@BindingAdapter("currency", "contactsCount", requireAll = false)
fun TextView.setCurrency(value: Double, contactsCount: Int = 1) {
    text = CurrencyFormat.format(value * max(contactsCount, 1))
}
