/*
 * Copyright 2022 Daniel Guger
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

package at.guger.moneybook.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import at.guger.moneybook.R
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Budget

/**
 * Provider for default items.
 */
object DataUtils {

    fun getDefaultAccount(context: Context) = Account(name = context.getString(R.string.Checking), color = Color.parseColor("#007d51"))

    fun getDefaultBudgets(context: Context): Array<Budget> = arrayOf(
        Budget(name = context.getString(R.string.Housekeeping), budget = 400.0, color = Color.parseColor("#BBDEFB")),
        Budget(name = context.getString(R.string.Leisure), budget = 200.0, color = Color.parseColor("#B39DDB")),
        Budget(name = context.getString(R.string.Shopping), budget = 100.0, color = Color.parseColor("#1976D2"))
    )

    const val MAX_ACCOUNTS = 8

    val ACCOUNT_COLORS: IntArray = intArrayOf(
        Color.parseColor("#007d51"),
        Color.parseColor("#37efba"),
        Color.parseColor("#1eb980"),
        Color.parseColor("#005d57"),
        Color.parseColor("#bae6d1"),
        Color.parseColor("#58c596"),
        Color.parseColor("#009d5f"),
        Color.parseColor("#005b30")
    )

    const val MAX_BUDGETS = 15

    fun getBudgetColors(context: Context): IntArray {
        return intArrayOf(
            ContextCompat.getColor(context, R.color.colorBudget1),
            ContextCompat.getColor(context, R.color.colorBudget2),
            ContextCompat.getColor(context, R.color.colorBudget3),
            ContextCompat.getColor(context, R.color.colorBudget4),
            ContextCompat.getColor(context, R.color.colorBudget5),
            ContextCompat.getColor(context, R.color.colorBudget6),
            ContextCompat.getColor(context, R.color.colorBudget7),
            ContextCompat.getColor(context, R.color.colorBudget8),
            ContextCompat.getColor(context, R.color.colorBudget9),
            ContextCompat.getColor(context, R.color.colorBudget10),
            ContextCompat.getColor(context, R.color.colorBudget11),
            ContextCompat.getColor(context, R.color.colorBudget12),
            ContextCompat.getColor(context, R.color.colorBudget13),
            ContextCompat.getColor(context, R.color.colorBudget14),
            ContextCompat.getColor(context, R.color.colorBudget15)
        )
    }
}