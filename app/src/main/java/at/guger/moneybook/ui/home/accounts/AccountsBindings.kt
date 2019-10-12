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

package at.guger.moneybook.ui.home.accounts

import androidx.databinding.BindingAdapter
import at.guger.moneybook.core.formatter.CurrencyFormat
import at.guger.moneybook.ui.home.ColoredAccount
import at.guger.strokepiechart.Entry
import at.guger.strokepiechart.StrokePieChart

/**
 * Binding adapters for the accounts screen.
 */

@BindingAdapter("accounts", requireAll = true)
fun StrokePieChart.setAccounts(accounts: List<ColoredAccount>) {
    val entries = ArrayList<Entry>()

    val balanceSum = accounts.sumByDouble { it.balance }

    for (account in accounts) {
        entries.add(Entry(if (balanceSum > 0.0) account.balance.toFloat() else 1.0f, account.color))
    }

    setEntries(entries)

    text = CurrencyFormat.format(balanceSum)

    startAnimation()
}