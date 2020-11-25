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

package at.guger.moneybook.util

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import at.guger.moneybook.R
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Account.Companion.DEFAULT_ACCOUNT_ID
import at.guger.moneybook.data.model.Budget

/**
 * Provider for default value items.
 */
object DataUtils {

    fun getDefaultAccount(context: Context) = Account(DEFAULT_ACCOUNT_ID, context.getString(R.string.Checking), Color.parseColor("#007d51"))

    fun getDefaultBudgets(context: Context): Array<Budget> = arrayOf(
        Budget(id = -100, name = context.getString(R.string.Housekeeping), budget = 400.0, color = Color.parseColor("#BBDEFB")),
        Budget(id = -99, name = context.getString(R.string.Leisure), budget = 200.0, color = Color.parseColor("#B39DDB")),
        Budget(id = -98, name = context.getString(R.string.Shopping), budget = 100.0, color = Color.parseColor("#1976D2"))
    )

    const val MAX_ACCOUNTS = 4

    val ACCOUNT_COLORS: Array<Int> = arrayOf(
        Color.parseColor("#007d51"),
        Color.parseColor("#37efba"),
        Color.parseColor("#1eb980"),
        Color.parseColor("#005d57")
    )

    const val MAX_BUDGETS = 9

    fun getBudgetColors(context: Context): IntArray {
        return intArrayOf(
            ContextCompat.getColor(context, R.color.budget1),
            ContextCompat.getColor(context, R.color.budget2),
            ContextCompat.getColor(context, R.color.budget3),
            ContextCompat.getColor(context, R.color.budget4),
            ContextCompat.getColor(context, R.color.budget5),
            ContextCompat.getColor(context, R.color.budget6),
            ContextCompat.getColor(context, R.color.budget7),
            ContextCompat.getColor(context, R.color.budget8),
            ContextCompat.getColor(context, R.color.budget9)
        )
    }
}