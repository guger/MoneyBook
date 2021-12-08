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

import android.animation.Animator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import at.guger.moneybook.core.util.ext.dp
import at.guger.moneybook.core.util.ext.sp
import kotlinx.coroutines.Job
import kotlin.math.max

class BudgetsInfoBarChart @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), MoneyBookBarChart {

    //region Variables

    private var waitingForSizeConfirmation: Boolean = false

    private val data = mutableListOf<BudgetDataPoint>()
    override val barHeights = mutableListOf<Float>()

    private var limitFactor: Int = 0
    private var limitPos: Float = 0.0f
    private var limitText: String

    private val barWidth = context.dp(BAR_WIDTH)
    private val barSpaceWidth = context.dp(BAR_SPACE_WIDTH)

    private val minBarLength = context.dp(MIN_BAR_LENGTH)

    private val topMargin = context.dp(CURVE_TOP_MARGIN)
    private val startMargin = context.dp(CURVE_START_MARGIN)
    private val endMargin = context.dp(CURVE_END_MARGIN)
    private val bottomMargin = context.dp(CURVE_BOTTOM_MARGIN)

    private val strokeLength = context.dp(STROKE_LENGTH)
    private val spaceLength = context.dp(SPACE_LENGTH)
    private val limitTextSize = context.sp(LIMIT_TEXT_SIZE)

    private val barPaths = mutableListOf<Path>()
    private val textPoints = mutableListOf<PointF>()
    private val limitPath = Path()

    private val barPaint: Paint
    private val limitPaint: Paint
    private val limitTextPaint: Paint

    private val barAnimator = MorphBarAnimator()
    private var pathAnimator: Animator? = null

    private var job: Job? = null

    //endregion

    //region Constructor

    init {
        clipToOutline = false

        barPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        limitPaint = Paint().apply {
            isAntiAlias = true
            color = ColorUtils.setAlphaComponent(Color.WHITE, LIMIT_ALPHA)
            strokeWidth = context.dp(LIMIT_BORDER_WIDTH)
            style = Paint.Style.STROKE
        }

        limitTextPaint = TextPaint().apply {
            isAntiAlias = true
            color = ColorUtils.setAlphaComponent(Color.WHITE, LIMIT_ALPHA)
            textSize = limitTextSize
        }

        limitText = formatPercent(100)

        setWillNotDraw(false)
    }

    //endregion

    //region View

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (data.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val w = MeasureSpec.getSize(widthMeasureSpec)
            val h = ((barWidth + 1.5 * limitTextSize) * data.size + barSpaceWidth * (data.size - 1) + topMargin + bottomMargin).toInt()

            setMeasuredDimension(w or MeasureSpec.EXACTLY, h or MeasureSpec.EXACTLY)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (waitingForSizeConfirmation) {
            setData(data)

            waitingForSizeConfirmation = false
        } else if (barHeights.isNotEmpty()) {
            computeBars()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (i in 0 until barPaths.size) {
            canvas?.drawPath(barPaths[i], barPaint.apply { color = data[i].color })
            canvas?.drawText(data[i].label, textPoints[i].x, textPoints[i].y, limitTextPaint)
        }

        canvas?.run {
            drawPath(limitPath, limitPaint)
            drawText(limitText, limitPos, context.dp(LIMIT_TEXT_SIZE), limitTextPaint)
        }
    }

    //endregion

    //region Methods

    fun setData(dataPoints: List<BudgetDataPoint>) {
        data.clear()
        data.addAll(dataPoints)

        if (!computeBars()) {
            waitingForSizeConfirmation = true
            return
        }

        requestLayout()
    }

    private fun computeBars(): Boolean {
        if (height * width == 0) return false

        barHeights.clear()

        val graphWidth = width - (startMargin + endMargin)

        val maxRatio = max(data.maxByOrNull { it.balance / it.budget }?.let { it.balance / it.budget } ?: 0.0f, 1.0f)

        limitFactor = 0
        do {
            limitFactor++
            limitPos = if (maxRatio > 0.0f) limitFactor * graphWidth / maxRatio else graphWidth
        } while (limitPos < width / 2)

        limitText = formatPercent(limitFactor * 100)

        for (bar in data) {
            val length = (limitPos / limitFactor) / bar.budget * bar.balance

            barHeights.add(max(length, minBarLength))
        }

        computeDecors()

        pathAnimator?.cancel()

        pathAnimator = barAnimator.getAnimation(this)

        pathAnimator?.start()

        return true
    }

    override fun computePath(barHeightValues: List<Float>) {
        if (barHeightValues.isEmpty()) return

        barPaths.clear()

        for (i in barHeightValues.indices) {
            val top = topMargin + limitTextSize + i * (barWidth + barSpaceWidth + 1.5f * limitTextSize)
            val path = Path().apply {
                moveTo(startMargin, top)
                addRect(startMargin, top, startMargin + barHeightValues[i], top + barWidth, Path.Direction.CW)
            }

            barPaths.add(path)

            textPoints.add(PointF(startMargin, top - limitTextSize / 2))
        }

        invalidate()
    }

    private fun computeDecors() {
        val sequenceLength = strokeLength + spaceLength
        val graphHeight = height - (topMargin + bottomMargin)

        with(limitPath)
        {
            reset()

            moveTo(startMargin + limitPos, topMargin)

            for (i in 0 until (graphHeight / sequenceLength + 1).toInt()) {
                lineTo(startMargin + limitPos, topMargin + i * sequenceLength + strokeLength)
                moveTo(startMargin + limitPos, topMargin + (i + 1) * sequenceLength)
            }
        }
    }

    private fun formatPercent(num: Int): String {
        return String.format("%d %%", num)
    }

    override fun setAnimationPath(animationPath: List<Path>) {
        barPaths.clear()
        barPaths.addAll(animationPath)

        invalidate()
    }

    //endregion

    companion object {
        const val LIMIT_ALPHA = 175

        const val BAR_WIDTH = 15.0f
        const val BAR_SPACE_WIDTH = 7.0f

        const val MIN_BAR_LENGTH = 5.0f

        const val LIMIT_BORDER_WIDTH = 0.75f

        const val STROKE_LENGTH = 7.5f
        const val SPACE_LENGTH = 2.0f

        const val LIMIT_TEXT_SIZE = 12.0f

        private const val CURVE_TOP_MARGIN = 20.0f
        private const val CURVE_START_MARGIN = 24.0f
        private const val CURVE_END_MARGIN = 24.0f
        private const val CURVE_BOTTOM_MARGIN = 36.0f
    }
}