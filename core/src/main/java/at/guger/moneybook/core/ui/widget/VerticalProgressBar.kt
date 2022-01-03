/*
 * Copyright 2022 Daniel Guger
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

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import at.guger.moneybook.core.R

/**
 * [View] displaying progress in percents.
 */
class VerticalProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    //region Variables

    var progress: Float = 0.0f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var progressColor: Int = Color.BLUE
        set(value) {
            field = value
            setupPaint()
            invalidate()
        }

    var animationDuration: Long = 300L

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animator: ValueAnimator? = null

    //endregion

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VerticalProgressBar)

        progress = typedArray.getFloat(R.styleable.VerticalProgressBar_progress, progress)
        progressColor = typedArray.getColor(R.styleable.VerticalProgressBar_progressColor, progressColor)

        typedArray.recycle()

        setupPaint()
    }

    //region View

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        val width = when (widthSpec) {
            MeasureSpec.AT_MOST -> DEFAULT_WIDTH
            MeasureSpec.EXACTLY -> measuredWidth
            else -> 0
        }

        val height = when (heightSpec) {
            MeasureSpec.AT_MOST -> DEFAULT_HEIGHT
            MeasureSpec.EXACTLY -> measuredHeight
            else -> 0
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawRect(0.0f, height - (height.toFloat() / 100.0f * progress), width.toFloat(), height.toFloat(), paint)
    }

    //endregion

    //region Methods

    fun startAnimation() {
        val currentProgress = progress

        animator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            duration = animationDuration
            interpolator = DecelerateInterpolator()

            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Float

                progress = currentProgress * value
                invalidate()
            }
            start()
        }
    }

    private fun setupPaint() {
        paint.color = progressColor
    }

    //endregion

    companion object {
        const val DEFAULT_WIDTH: Int = 4
        const val DEFAULT_HEIGHT: Int = 50
    }
}