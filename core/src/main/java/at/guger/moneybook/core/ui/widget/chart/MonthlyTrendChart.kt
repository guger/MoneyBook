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

package at.guger.moneybook.core.ui.widget.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import at.guger.moneybook.core.R
import at.guger.moneybook.core.util.ext.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MonthlyTrendChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    //region Variables

    private val data = mutableListOf<DateDataPoint>()
    private val points = mutableListOf<PointF>()

    private val topMargin = CURVE_TOP_MARGIN
    private val bottomMargin = CURVE_BOTTOM_MARGIN

    private val path = Path()

    private val pathPaint: Paint

    private var job: Job? = null

    //endregion

    //region Constructor

    init {
        clipToOutline = false

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MonthlyTrendChart)

        val barColor = ta.getColor(R.styleable.MonthlyTrendChart_barColor, Color.GRAY)
        val borderColor = ta.getColor(R.styleable.MonthlyTrendChart_curveBorderColor, Color.parseColor("#ff21AF6C"))
        val fillColor = ta.getColor(R.styleable.MonthlyTrendChart_curveFillColor, ColorUtils.setAlphaComponent(borderColor, 25))

        ta.recycle()

        pathPaint = Paint().apply {
            isAntiAlias = true
            color = borderColor
            strokeWidth = context.dp(CURVE_BORDER_WIDTH)
            style = Paint.Style.STROKE
        }

        setWillNotDraw(false)
    }

    //endregion

    //region View

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        println("MB: onSizeChanged: ${data.size}")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        computePath()

        canvas?.drawPath(path, pathPaint)
    }

    //endregion

    //region Methods

    fun setDataPoints(dataPoints: List<DateDataPoint>) {
        job = GlobalScope.launch(Dispatchers.Main) {
            job?.cancel()

            val min = dataPoints.minByOrNull { it.value }?.value ?: 0.0f

            val positiveDataPoints: List<DateDataPoint> = List(dataPoints.size) { i -> DateDataPoint(dataPoints[i].date, dataPoints[i].value - min) }

            data.clear()
            data.addAll(positiveDataPoints)

            computePoints()

            postInvalidate()

            println("MB: invalidate: ${data.size}")
        }
    }

    fun setCurveBorderColor(@ColorInt color: Int) {
        pathPaint.color = color
    }

    private fun computePoints() {
        points.clear()

        val graphHeight = height.toFloat() - (topMargin + bottomMargin)

        val maxValue: Float = data.maxByOrNull { it.value }!!.value

        val xStep: Float = width.toFloat() / (data.size - 1)

        for (i in 0 until data.size) {
            val xVal = xStep * i
            val yVal = graphHeight + topMargin - data[i].value / maxValue * graphHeight

            points.add(PointF(xVal, yVal))
        }
    }

    private fun computePath() {
        if (points.isEmpty()) return

        with(path) {
            reset()

            moveTo(points.first().x, points.first().y)

            for (i in 0 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
    }

    //endregion

    companion object {
        const val BAR_WIDTH = 0.5f
        const val CURVE_BORDER_WIDTH = 3.5f

        private const val CURVE_TOP_MARGIN = 36.0f
        private const val CURVE_BOTTOM_MARGIN = 48.0f
    }
}