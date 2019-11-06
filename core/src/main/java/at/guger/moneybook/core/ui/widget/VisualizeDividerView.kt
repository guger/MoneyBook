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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import at.guger.moneybook.core.R
import at.guger.moneybook.core.util.Constants
import kotlin.math.abs
import kotlin.math.max


/**
 * Divider item visualizing distributions in a bar.
 */
class VisualizeDividerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    //region Variables

    private var colors: List<Int>
    private var distributions: List<Float>

    private val paint: Paint

    //endregion

    //region Constructor

    init {
        val typedValue = TypedValue()
        val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimary))
        colors = listOf(a.getColor(0, 0))
        a.recycle()

        distributions = listOf(100.0f)

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    //endregion

    //region View

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        paint.strokeWidth = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in distributions.indices) {
            paint.color = colors[i]

            canvas.drawRect(
                width / 100.0f * distributions.subList(0, i).sum(),
                0.0f,
                width / 100.0f * distributions.subList(0, i.inc()).sum(),
                height.toFloat(),
                paint
            )
        }
    }

    //endregion

    //region Methods

    /**
     * Set the distributions together with their colors.
     *
     * @param distributions An array of distributions.
     * @param colors An array of colors for the corresponding distribution.
     */
    fun setDistributions(distributions: List<Float>, colors: List<Int>? = null, colorsRes: List<Int>? = null) {
        require(colors != null || colorsRes != null)

        val colorList: MutableList<Int> = (colors ?: List(max(colorsRes!!.size, 1)) { i -> ContextCompat.getColor(context, colorsRes[i]) }).toMutableList()

        if (colorList.isEmpty()) colorList.add(Color.TRANSPARENT)

        val distributionsSum = distributions.sum()

        val percents = List(max(distributions.size, 1)) { i ->
            if (distributionsSum > 0) {
                100.0f / distributionsSum * distributions[i]
            } else {
                100.0f / max(distributions.size, 1)
            }
        }

        setDistributionPercents(percents, colorList)
    }

    /**
     * Set the distributions together with their colors.
     *
     * @param distributions An array of distributions in %, when summed 100%.
     * @param colors An array of colors for the corresponding distribution.
     */
    fun setDistributionPercents(distributions: List<Float>, colors: List<Int>) {
        require(colors.size == distributions.size)
        require(abs(distributions.sum() - 100) < Constants.EPSILON)

        this.colors = colors
        this.distributions = distributions

        invalidate()
    }

    //endregion
}