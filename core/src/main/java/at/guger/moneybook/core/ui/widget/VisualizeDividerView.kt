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
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import at.guger.moneybook.core.R


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
     * @param colors An array with colors for the corresponding distribution.
     * @param distributions An array of distributions in %, when summed 100%.
     */
    fun setColorDistribution(colors: List<Int>, distributions: List<Float>) {
        require(colors.size == distributions.size)
        require(distributions.sum() == 100.0f)

        this.colors = colors
        this.distributions = distributions

        invalidate()
    }

    //endregion
}