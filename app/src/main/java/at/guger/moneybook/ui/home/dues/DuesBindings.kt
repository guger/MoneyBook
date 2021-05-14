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

package at.guger.moneybook.ui.home.dues

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.widget.StrokePieChart
import at.guger.moneybook.core.util.ext.size
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.util.CurrencyFormat
import kotlin.math.max

/**
 * Binding adapters for the dues screen.
 */

@BindingAdapter("dues")
fun StrokePieChart.setDues(dues: List<Transaction>?) {
    val claimsSum = dues?.filter { !it.isPaid && it.type == Transaction.TransactionType.CLAIM }?.sumOf { it.value * max(it.contacts.size(), 1) } ?: 0.0
    val debtsSum = dues?.filter { !it.isPaid && it.type == Transaction.TransactionType.DEBT }?.sumOf { it.value * max(it.contacts.size(), 1) } ?: 0.0

    setEntries(
        arrayListOf(
            StrokePieChart.Entry(if (claimsSum + debtsSum > 0.0) claimsSum.toFloat() else 1.0f, ContextCompat.getColor(context, R.color.color_claim)),
            StrokePieChart.Entry(if (claimsSum + debtsSum > 0.0) debtsSum.toFloat() else 1.0f, ContextCompat.getColor(context, R.color.color_debt))
        )
    )

    text = CurrencyFormat.formatShortened(context, claimsSum - debtsSum)

    startAnimation()
}