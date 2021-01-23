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

package at.guger.moneybook.util

import android.content.Context
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import org.koin.core.KoinComponent
import org.koin.core.get
import java.text.NumberFormat

/**
 * Formatter for currency instances.
 */
object CurrencyFormat : KoinComponent {

    private val currencyFormatShort = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        currency = get<Preferences>().currency
    }
    private val currencyFormatLong = NumberFormat.getCurrencyInstance().apply {
        currency = get<Preferences>().currency
    }

    @JvmStatic
    fun format(value: Double): String = currencyFormatLong.format(value)

    fun formatShortened(context: Context, value: Double): String {
        return when {
            value < 10_000 -> currencyFormatLong.format(value)
            value < 1_000_000 -> currencyFormatShort.format(value)
            else -> context.getString(R.string.x_Mio, currencyFormatLong.format(value / 1_000_000))
        }
    }
}