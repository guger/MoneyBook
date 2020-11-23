/*
 * Copyright 2020 Daniel Guger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.guger.moneybook.core.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import at.guger.moneybook.core.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Line Graph Chart for displaying a trend of transactions.
 */
class LineGraphChart : View {

    //region Variables

    private val data = mutableListOf<DataPoint>()
    private val points = mutableListOf<PointF>()
    private val conPoint1 = mutableListOf<PointF>()
    private val conPoint2 = mutableListOf<PointF>()

    private val path = Path()
    private val borderPath = Path()
    private val barPaint = Paint()
    private val pathPaint = Paint()
    private val borderPathPaint = Paint()

    private var viewCanvas: Canvas? = null
    private var bitmap: Bitmap? = null
    private val bitmapPaint = Paint(Paint.DITHER_FLAG)

    private var curveTopMargin = 32

    private val barWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, .5f, resources.displayMetrics
        )
    }

    private val borderPathWidth by lazy {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 3.5f, resources.displayMetrics
        )
    }

    //endregion

    //region Constructor

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(set: AttributeSet?) {
        val ta = context.obtainStyledAttributes(set, R.styleable.LineGraphChart)
        val barColor = ta.getColor(R.styleable.LineGraphChart_barColor, Color.GRAY)
        val borderColor = ta.getColor(R.styleable.LineGraphChart_curveBorderColor, Color.parseColor("#ff21AF6C"))
        val fillColor = ta.getColor(R.styleable.LineGraphChart_curveFillColor, ColorUtils.setAlphaComponent(borderColor, 25))


        curveTopMargin = ta.getDimensionPixelSize(R.styleable.LineGraphChart_curveTopMargin, 0)
        ta.recycle()

        barPaint.apply {
            isAntiAlias = true
            strokeWidth = barWidth
            style = Paint.Style.STROKE
            color = barColor
        }
        pathPaint.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = fillColor
        }
        borderPathPaint.apply {
            isAntiAlias = true
            strokeWidth = borderPathWidth
            style = Paint.Style.STROKE
            color = borderColor
        }
    }

    //endregion

    //region View

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        viewCanvas = Canvas(bitmap!!)
        drawVerticalBars(viewCanvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawBezierCurve(canvas)
        bitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, bitmapPaint)
        }
    }

    //endregion

    //region Methods

    private fun drawVerticalBars(canvas: Canvas?) {
        val largeBarHeight = getLargeBarHeight()
        val smallBarHeight = height - largeBarHeight / 4

        val barsCount = if (data.isNotEmpty()) data.size else VERTICAL_BARS

        val barMargin = (width - (barWidth * barsCount)) / barsCount
        var startX = 0f
        val startY = height.toFloat()
        var endX: Float
        var endY: Float

        for (i in 0 until barsCount) {
            startX += barWidth + barMargin
            endX = startX
            endY = if (data.isNotEmpty() && data[i].date.dayOfWeek != DayOfWeek.MONDAY || i % INDEX_OF_LARGE_BAR != 2) {
                smallBarHeight
            } else {
                largeBarHeight
            }
            canvas?.drawLine(startX, startY, endX, endY, barPaint)
        }
    }

    private fun drawBezierCurve(canvas: Canvas?) {
        try {
            if (points.isEmpty() && conPoint1.isEmpty() && conPoint2.isEmpty()) return

            path.reset()
            path.moveTo(points.first().x, points.first().y)

            for (i in 1 until points.size) {
                path.cubicTo(
                    conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                    points[i].x, points[i].y
                )
            }

            borderPath.set(path)

            path.lineTo(width.toFloat(), height.toFloat())
            path.lineTo(0f, height.toFloat())

            canvas?.drawPath(path, pathPaint)

            canvas?.drawPath(borderPath, borderPathPaint)
        } catch (e: Exception) {
        }
    }

    private fun calculatePointsForData() {
        if (data.isEmpty()) return

        val bottomY = getLargeBarHeight() - CURVE_BOTTOM_MARGIN
        val xDiff = width.toFloat() / (data.size - 1) //subtract -1 because we want to include position at right side

        val maxData = data.maxByOrNull { it.value }!!.value

        for (i in 0 until data.size) {
            val y = bottomY - (data[i].value / maxData * (bottomY - curveTopMargin))
            points.add(PointF(xDiff * i, y))
        }
    }

    private fun calculateConnectionPointsForBezierCurve() {
        try {
            for (i in 1 until points.size) {
                conPoint1.add(PointF((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
                conPoint2.add(PointF((points[i].x + points[i - 1].x) / 2, points[i].y))
            }
        } catch (e: Exception) {
        }
    }

    private fun getLargeBarHeight() = height / 4 * 3f

    fun addDataPoints(data: List<DataPoint>) {
        //do calculation in worker thread // Note: You should use some safe thread mechanism
        //Calculation logic here are not fine, should updated when more time available
        post {
            GlobalScope.launch(Dispatchers.IO) {
                val min = data.minByOrNull { it.value }?.value?.takeUnless { it >= 0f }

                val dataPoints: List<DataPoint> = if (min != null) {
                    List(data.size) { i -> DataPoint(data[i].date, data[i].value - min) }
                } else {
                    data
                }

                val oldPoints = points.toList()

                if (oldPoints.isEmpty()) {
                    this@LineGraphChart.data.addAll(dataPoints.toList())
                    calculatePointsForData()
                    calculateConnectionPointsForBezierCurve()
                    postInvalidate()
                    return@launch
                }

                resetDataPoints()
                this@LineGraphChart.data.addAll(dataPoints.toList())
                calculatePointsForData()
                calculateConnectionPointsForBezierCurve()

                val newPoints = points.toList()

                val size = min(oldPoints.size, newPoints.size)
                var maxDiffY = 0f
                for (i in 0 until size) {
                    val abs = abs(oldPoints[i].y - newPoints[i].y)
                    if (abs > maxDiffY) maxDiffY = abs
                }

                val loopCount = maxDiffY / 16

                val tempPointsForAnimation = mutableListOf<MutableList<PointF>>()

                for (i in 0 until size) {
                    val old = oldPoints[i]
                    val new = newPoints[i]

                    val plusOrMinusAmount = abs(new.y - old.y) / maxDiffY * 16

                    var tempY = old.y
                    val tempList = mutableListOf<PointF>()

                    for (j in 0..loopCount.toInt()) {
                        if (tempY == new.y) {
                            tempList.add(PointF(new.x, new.y))

                        } else {

                            if (new.y > old.y) {
                                tempY += plusOrMinusAmount
                                tempY = min(tempY, new.y)
                                tempList.add(PointF(new.x, tempY))

                            } else {
                                tempY -= plusOrMinusAmount
                                tempY = max(tempY, new.y)
                                tempList.add(PointF(new.x, tempY))
                            }
                        }
                    }
                    tempPointsForAnimation.add(tempList)

                }

                if (tempPointsForAnimation.isEmpty()) return@launch

                val first = tempPointsForAnimation[0]
                val length = first.size

                for (i in 0 until length) {
                    conPoint1.clear()
                    conPoint2.clear()
                    points.clear()
                    points.addAll(tempPointsForAnimation.map { it[i] })
                    calculateConnectionPointsForBezierCurve()
                    postInvalidate()
                    delay(6)
                }
            }
        }
    }

    private fun resetDataPoints() {
        this.data.clear()
        points.clear()
        conPoint1.clear()
        conPoint2.clear()
    }

    fun setCurveBorderColor(@ColorRes color: Int) {
        borderPathPaint.color = ContextCompat.getColor(context, color)
    }

    //endregion

    companion object {
        private const val INDEX_OF_LARGE_BAR = 6
        private const val VERTICAL_BARS = (INDEX_OF_LARGE_BAR * INDEX_OF_LARGE_BAR) + 6 // add fixed bars size
        private const val CURVE_BOTTOM_MARGIN = 48.0f
    }

    data class DataPoint(val date: LocalDate, val value: Float)
}