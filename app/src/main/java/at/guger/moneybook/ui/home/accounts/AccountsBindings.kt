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

import androidx.databinding.BindingAdapter
import at.guger.moneybook.core.ui.widget.StrokePieChart
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.util.CurrencyFormat

/**
 * Binding adapters for the accounts screen.
 */

@BindingAdapter("accounts", requireAll = true)
fun StrokePieChart.setAccounts(accounts: List<AccountWithBalance>) {
    val entries = ArrayList<StrokePieChart.Entry>()

    val balanceSum = accounts.sumOf { it.balance + it.startBalance }

    accounts.forEach {
        entries.add(
            StrokePieChart.Entry(
                if (balanceSum > 0.0) (it.balance + it.startBalance).toFloat() else 1.0f,
                it.color
            )
        )
    }

    setEntries(entries)

    text = CurrencyFormat.formatShortened(context, balanceSum)

    startAnimation()
}