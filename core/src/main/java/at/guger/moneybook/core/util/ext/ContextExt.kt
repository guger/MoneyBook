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

package at.guger.moneybook.core.util.ext

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes

/**
 * Extension functions for [Context].
 */

/**
 * Converts the given (dp) value to pixels.
 *
 * @param value The value in dp.
 *
 * @return The given value converted to pixels.
 */
fun Context.dp(value: Int): Float {
    return TypedValue.applyDimension(COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics)
}

/**
 * Resolves the value of the given dimension resource or attribute.
 *
 * @param res The dimension resource id.
 * @param attr The dimension attribute id.
 *
 * @return The value of the given dimension.
 */
fun Context.dimen(
    @DimenRes res: Int? = null,
    @AttrRes attr: Int? = null,
    fallback: Float = 0.0f
): Float {
    require(attr != null || res != null)
    if (res != null) {
        return resources.getDimension(res)
    }
    requireNotNull(attr)
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    try {
        return a.getDimension(0, fallback)
    } finally {
        a.recycle()
    }
}

/**
 * Resolves the color of a given attribute.
 *
 * @param attr The attribute of the color.
 *
 * @return The color as Integer.
 */
@ColorInt
fun Context.resolveColor(colorAttr: Int): Int {
    val typedValue = TypedValue()
    val ta = obtainStyledAttributes(typedValue.data, intArrayOf(colorAttr))

    return ta.getColor(0, Color.BLACK).also { ta.recycle() }
}
