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
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.text.NumberFormat
import java.util.*

/**
 * Formatter for currency instances.
 */
object CurrencyFormat : KoinComponent {

    private val currencyFormatShort = NumberFormat.getCurrencyInstance().apply {
        maximumFractionDigits = 0
        get<Preferences>().currency?.let { currency = it } ?: Firebase.crashlytics.log("Default locale not available: ${Currency.getAvailableCurrencies().size} available")
    }

    private val currencyFormatLong = NumberFormat.getCurrencyInstance().apply {
        get<Preferences>().currency?.let { currency = it } ?: logException()
    }

    private fun logException() {
        Firebase.crashlytics.run {
            setCustomKey(CrashlyticsKeys.KEY_AVAILABLE_CURRENCIES, Currency.getAvailableCurrencies().size)
            log("Default locale not available.")
        }
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