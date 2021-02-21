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
import kotlinx.coroutines.Job
import java.time.DayOfWeek

class MonthlyTrendChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    //region Variables

    private var waitingForSizeConfirmation: Boolean = false

    private val data = mutableListOf<DateDataPoint>()
    private val points = mutableListOf<PointF>()
    private val conPoints1 = mutableListOf<PointF>()
    private val conPoints2 = mutableListOf<PointF>()

    private val barWidth = context.dp(BAR_WIDTH)
    private val topMargin = CURVE_TOP_MARGIN
    private val bottomMargin = CURVE_BOTTOM_MARGIN

    private val curvePath = Path()
    private val fillPath = Path()

    private val barPaint: Paint
    private val curvePaint: Paint
    private val fillPaint: Paint

    private var job: Job? = null

    //endregion

    //region Constructor

    init {
        clipToOutline = false

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MonthlyTrendChart)

        val barColor = ta.getColor(R.styleable.MonthlyTrendChart_barColor, Color.WHITE)
        val borderColor = ta.getColor(R.styleable.MonthlyTrendChart_curveBorderColor, Color.BLACK)

        ta.recycle()

        barPaint = Paint().apply {
            isAntiAlias = true
            color = barColor
            strokeWidth = barWidth
            style = Paint.Style.STROKE
        }

        curvePaint = Paint().apply {
            isAntiAlias = true
            color = borderColor
            strokeWidth = context.dp(CURVE_BORDER_WIDTH)
            style = Paint.Style.STROKE
        }

        fillPaint = Paint().apply {
            isAntiAlias = true
            color = ColorUtils.setAlphaComponent(borderColor, FILL_ALPHA)
            style = Paint.Style.FILL
        }

        setWillNotDraw(false)
    }

    //endregion

    //region View

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (waitingForSizeConfirmation) {
            setDataPoints(data)

            waitingForSizeConfirmation = false
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        computePath()

        drawBars(canvas)

        canvas?.drawPath(curvePath, curvePaint)
        canvas?.drawPath(fillPath, fillPaint)
    }

    //endregion

    //region Methods

    fun setDataPoints(dataPoints: List<DateDataPoint>) {
        val min = dataPoints.minByOrNull { it.value }?.value

        val positiveDataPoints: List<DateDataPoint> = if (min != null) {
            List(dataPoints.size) { i -> DateDataPoint(dataPoints[i].date, dataPoints[i].value - min) }
        } else {
            dataPoints.toList()
        }

        data.clear()
        data.addAll(positiveDataPoints)

        if (!computePoints()) {
            waitingForSizeConfirmation = true
            return
        }

        postInvalidate()
    }

    fun setCurveBorderColor(@ColorInt color: Int) {
        curvePaint.color = color
        fillPaint.color = ColorUtils.setAlphaComponent(color, FILL_ALPHA)
    }

    private fun computePoints(): Boolean {
        if (height * width == 0) return false

        points.clear()
        conPoints1.clear()
        conPoints2.clear()

        val graphHeight = largeBarHeight - (topMargin + bottomMargin)

        val maxValue: Float = data.maxByOrNull { it.value }!!.value

        val xStep: Float = width.toFloat() / (data.size - 1)

        for (i in 0 until data.size) {
            val xVal = i * xStep
            val yVal = graphHeight + topMargin - data[i].value / maxValue * graphHeight

            points.add(PointF(xVal, yVal))

            if (i == 0 || i == data.size - 1) points.add(PointF(xVal, yVal))
        }

        for (i in 0 until points.size) {
            if (i == 0) {
                conPoints1.add(PointF(points[i].x, points[i].y))
                conPoints2.add(PointF(points[i].x, points[i].y))
            } else {
                conPoints1.add(PointF((points[i - 1].x + points[i].x) / 2, points[i - 1].y))
                conPoints2.add(PointF((points[i - 1].x + points[i].x) / 2, points[i].y))
            }
        }

        return true
    }

    private val smallBarHeight: Float
        get() = height - largeBarHeight / 4

    private val largeBarHeight: Float
        get() = height / 4 * 3.0f

    private fun drawBars(canvas: Canvas?) {
        val barsCount = data.size

        val xStep: Float = (width.toFloat() - (barsCount + 1) * barWidth) / (barsCount + 1)

        for (i in 0 until barsCount) {
            val xPos = xStep + i * (xStep + barWidth)
            val yPos = if (data[i].date.dayOfWeek == DayOfWeek.MONDAY) largeBarHeight else smallBarHeight

            canvas?.drawLine(xPos, height.toFloat(), xPos, yPos, barPaint)
        }
    }

    private fun computePath() {
        if (points.isEmpty()) return

        with(fillPath) {
            reset()

            moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                cubicTo(conPoints1[i].x, conPoints1[i].y, conPoints2[i].x, conPoints2[i].y, points[i].x, points[i].y)
            }
        }

        curvePath.set(fillPath)

        with(fillPath) {
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0.0f, height.toFloat())
        }
    }

    //endregion

    companion object {
        const val FILL_ALPHA = 25

        const val BAR_WIDTH = 0.5f
        const val CURVE_BORDER_WIDTH = 3.5f

        private const val CURVE_TOP_MARGIN = 36.0f
        private const val CURVE_BOTTOM_MARGIN = 48.0f
    }
}