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

package at.guger.moneybook.core.ui.transition.helper

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.FloatRange
import com.google.android.material.shape.ShapeAppearanceModel
import kotlin.math.roundToInt

class CornerRounding(
    val topLeftRadius: Float = 0f,
    val topRightRadius: Float = 0f,
    val bottomRightRadius: Float = 0f,
    val bottomLeftRadius: Float = 0f
)

// To FloatArray suitable for Path#addRoundRect
fun CornerRounding.toFloatArray(): FloatArray {
    return floatArrayOf(
        topLeftRadius, topLeftRadius,
        topRightRadius, topRightRadius,
        bottomRightRadius, bottomRightRadius,
        bottomLeftRadius, bottomLeftRadius
    )
}

fun ShapeAppearanceModel?.toCornerRounding(bounds: RectF): CornerRounding {
    if (this == null) return CornerRounding()
    return CornerRounding(
        topLeftCornerSize.getCornerSize(bounds),
        topRightCornerSize.getCornerSize(bounds),
        bottomRightCornerSize.getCornerSize(bounds),
        bottomLeftCornerSize.getCornerSize(bounds)
    )
}

private val boundsRectF = RectF()
fun Canvas.withAlpha(
    bounds: Rect,
    @FloatRange(from = 0.0, fromInclusive = true, to = 1.0, toInclusive = true) alpha: Float,
    block: Canvas.() -> Unit
) {
    boundsRectF.set(bounds)
    val checkpoint  = saveLayerAlpha(boundsRectF, (alpha * 255).roundToInt())
    block()
    restoreToCount(checkpoint)
}
