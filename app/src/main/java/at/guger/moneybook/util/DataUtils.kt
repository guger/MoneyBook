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

package at.guger.moneybook.util

import android.content.Context
import android.graphics.Color
import at.guger.moneybook.R
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Account.Companion.DEFAULT_ACCOUNT_ID
import at.guger.moneybook.data.model.Budget

/**
 * Provider for default value items.
 */
object DataUtils {

    fun getDefaultAccount(context: Context) = Account(DEFAULT_ACCOUNT_ID, context.getString(R.string.Checking))

    fun getDefaultBudgets(context: Context): Array<Budget> = arrayOf(
        Budget(-100, context.getString(R.string.Housekeeping), color = Color.parseColor("#795548")),
        Budget(id = -99, name = context.getString(R.string.Leisure), color = Color.parseColor("#1e88e5")),
        Budget(id = -98, name = context.getString(R.string.Shopping), color = Color.parseColor("#00bcd4"))
    )

    fun getAccountColors(): Array<Int> {
        return arrayOf(
            Color.parseColor("#37efba"),
            Color.parseColor("#1eb980"),
            Color.parseColor("#007d51"),
            Color.parseColor("#005d57")
        )
    }
}