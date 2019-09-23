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

package at.guger.moneybook.ui.home.overview

import android.widget.TextView
import androidx.databinding.BindingAdapter
import at.guger.moneybook.core.formatter.CurrencyFormat
import at.guger.moneybook.data.model.Transaction

/**
 * Binding adapters for the overview screen.
 */

@BindingAdapter("transactions", requireAll = true)
fun TextView.setTransactions(transactions: List<Transaction>?) {
    transactions?.let {
        setCurrency(it.sumByDouble { transaction ->
            when (transaction.type) {
                Transaction.TransactionType.EARNING -> transaction.value
                else -> transaction.value.unaryMinus()
            }
        })
    }
}

@BindingAdapter("currency", requireAll = true)
fun TextView.setCurrency(value: Double) {
    text = CurrencyFormat.format(value)
}
