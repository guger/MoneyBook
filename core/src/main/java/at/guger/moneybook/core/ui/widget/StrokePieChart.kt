/*
 * Copyright 2021 Daniel Guger
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

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import at.guger.moneybook.core.R
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Circular stroked pie chart.
 */
class StrokePieChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    //region Variables

    private val defaultSize: Int
    private val density: Float
    private var entriesSum: Float = 0.0f
    private var animator: ValueAnimator? = null

    var animationDuration: Long = 500

    /**
     * The distance between two entries in degrees.
     */
    var distance: Float = 3.5f
        set(value) {
            field = value
            invalidate()
        }

    private var currentDistance: Float = distance

    var roundEdges: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    var strokeWidth: Float = 6.0f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var color: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var text: String? = null
        set(value) {
            field = value
            invalidate()
        }
    var textSize: Float?

    @ColorInt
    var textColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }
    var typeface: Typeface? = null
        set(value) {
            field = value
            invalidate()
        }
    var isBold: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val chartRect: RectF = RectF()

    private var entries = mutableListOf<Float>()
    private var colors = mutableListOf<Int>()

    //endregion

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StrokePieChart)

        density = context.resources.displayMetrics.density
        defaultSize = (DEFAULT_SIZE * density).roundToInt()
        strokeWidth = typedArray.getDimension(R.styleable.StrokePieChart_strokeWidth, strokeWidth * density)
        roundEdges = typedArray.getBoolean(R.styleable.StrokePieChart_roundedEdges, false)
        text = typedArray.getString(R.styleable.StrokePieChart_text)
        textSize = typedArray.getDimension(R.styleable.StrokePieChart_textSize, 0.0f).takeIf { it > 0 }
        textColor = typedArray.getColor(R.styleable.StrokePieChart_textColor, Color.WHITE)

        val fontId = typedArray.getResourceId(R.styleable.StrokePieChart_fontResId, -1)
        typeface = fontId.takeIf { it > 0 }?.let { ResourcesCompat.getFont(context, it) }

        isBold = typedArray.getBoolean(R.styleable.StrokePieChart_bold, false)

        color = typedArray.getColor(R.styleable.StrokePieChart_defaultColor, Color.BLACK)

        distance = typedArray.getFloat(R.styleable.StrokePieChart_distance, distance)

        typedArray.recycle()
    }

    //region View

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        val width = when (widthSpec) {
            MeasureSpec.AT_MOST -> defaultSize
            MeasureSpec.EXACTLY -> measuredWidth
            else -> 0
        }

        val height = when (heightSpec) {
            MeasureSpec.AT_MOST -> defaultSize
            MeasureSpec.EXACTLY -> measuredHeight
            else -> 0
        }

        val chartSize = min(width, height)
        val centerX = (width - chartSize) / 2
        val centerY = (height - chartSize) / 2
        val strokeHalf = strokeWidth / 2

        chartRect.set(
            centerX + strokeHalf,
            centerY + strokeHalf,
            centerX + chartSize - strokeHalf,
            centerY + chartSize - strokeHalf
        )
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (entries.isNotEmpty()) {
            check(colors.size == entries.size) { "Color and Stats list must have the same size." }

            entries.forEachIndexed { index, value ->
                drawChart(
                    canvas,
                    colors[index],
                    if (index == 0) 0.0f else entries.subList(0, index).sum(),
                    value
                )
            }
        } else {
            drawChart(canvas, color, 0.0f, FULL_CIRCLE_ANGLE)
        }

        drawText(canvas)
    }

    //endregion

    //region Methods

    fun setEntries(entriesList: List<Entry>) {
        val sortedEntries = entriesList.filter { it.value > 0 }.sortedBy { it.value }

        entriesSum = sortedEntries.sumOf { it.value.toDouble() }.toFloat()

        animator?.cancel()

        entries = mutableListOf()
        colors = mutableListOf()

        currentDistance = distance

        val distanceSum = sortedEntries.size * distance

        for (entry in sortedEntries) {
            entries.add((FULL_CIRCLE_ANGLE - distanceSum) / (entriesSum) * entry.value)
            colors.add(entry.color)
            entries.add(distance)
            colors.add(Color.TRANSPARENT)
        }

        invalidate()
    }

    fun startAnimation() {
        val animatedEntries = entries.toList()

        val animatedDistance = currentDistance

        animator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()

            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float

                entries.forEachIndexed { index, _ ->
                    entries[index] = animatedEntries[index] * value
                    currentDistance = animatedDistance * value
                    invalidate()
                }
            }
            start()
        }
    }

    private fun drawChart(canvas: Canvas?, @ColorInt color: Int, start: Float, degree: Float) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
        if (roundEdges) paint.strokeCap = Paint.Cap.ROUND

        val base = if (entries.size <= 2) 90.0f else 270.0f

        canvas?.drawArc(chartRect, base + start, degree, false, paint)
    }

    private fun drawText(canvas: Canvas?) {
        text?.let {
            val paint = Paint()
            paint.color = textColor
            paint.textSize = textSize ?: chartRect.height() / 3
            paint.typeface = typeface ?: Typeface.DEFAULT_BOLD
            paint.isFakeBoldText = isBold
            paint.isAntiAlias = true

            val textBounds = Rect()
            paint.getTextBounds(it, 0, it.length, textBounds)
            paint.measureText(it)

            canvas?.drawText(it, (((width - textBounds.right) / 2).toFloat()), ((height + textBounds.height()) / 2).toFloat(), paint)
        }
    }

    //endregion

    data class Entry(val value: Float, @ColorInt val color: Int)

    companion object {
        private const val DEFAULT_SIZE = 100
        private const val FULL_CIRCLE_ANGLE = 360.0f
    }
}