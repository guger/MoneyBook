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

package at.guger.moneybook.core.ui.widget

import android.content.Context
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import at.guger.moneybook.core.R
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * [TextInputEditText] with some additional functions to display a currency value.
 */
class CurrencyTextInputEditText @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle) :
    TextInputEditText(context, attrs, defStyleAttr) {

    init {
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL

        keyListener = DigitsKeyListener.getInstance("01234567890,.")
    }

    fun isValidNumber(): Boolean = text?.matches("""\d*[.,]?\d*""".toRegex()) == true

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)

        setSelection(text?.length ?: 0)
    }

    fun setDecimalValue(value: Double) {
        setText(CURRENCY_FORMAT.format(value))
    }

    fun getDecimalNumber() = parseNumber(text?.toString())

    private fun parseNumber(text: String?) = text?.replace(",", ".")?.toDoubleOrNull() ?: 0.0


    companion object {
        val CURRENCY_FORMAT: NumberFormat = DecimalFormat.getInstance().apply {
            isGroupingUsed = false
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }
}