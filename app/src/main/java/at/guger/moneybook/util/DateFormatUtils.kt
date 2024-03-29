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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.ResolverStyle

/**
 * Utils for Transaction/Due items date format.
 */
object DateFormatUtils {

    val SHORT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withResolverStyle(ResolverStyle.LENIENT)
    const val MMM_YYYY_DATE_FORMAT: String = "MMM yyyy"

    @JvmStatic
    fun formatTransactionDate(date: LocalDate): String = SHORT_DATE_FORMAT.format(date)

    @JvmStatic
    fun formatDueDate(context: Context, date: LocalDate) = context.getString(R.string.Due_x, SHORT_DATE_FORMAT.format(date))
}