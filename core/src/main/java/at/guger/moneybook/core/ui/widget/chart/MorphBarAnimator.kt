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
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import androidx.annotation.IntRange
import androidx.core.animation.doOnEnd

class MorphBarAnimator : Animator(), ChartAnimator {

    //region Variables

    private val animator: ValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
    private val oldHeights = mutableListOf<Float>()

    //endregion

    //region Methods

    override fun getAnimation(barChart: MoneyBookBarChart): Animator? {
        val barHeights = barChart.barHeights

        if (barHeights.isEmpty()) return null

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float

            val steps = List(barHeights.size) { i -> barHeights[i] - (oldHeights.getOrNull(i) ?: 0.0f) }
            val currentBarHeights = List(steps.size) { i -> animatedValue * steps[i] + (oldHeights.getOrNull(i) ?: 0.0f) }

            barChart.computePath(currentBarHeights)
        }

        animator.doOnEnd {
            oldHeights.clear()
            oldHeights.addAll(barHeights)
        }

        return animator;
    }

    override fun getStartDelay(): Long = animator.startDelay

    override fun setStartDelay(@IntRange(from = 0) delay: Long) {
        animator.startDelay = delay
    }

    override fun getDuration(): Long = animator.duration

    override fun setDuration(@IntRange(from = 0) dur: Long): Animator {
        return animator
    }

    override fun setInterpolator(timeInterpolator: TimeInterpolator?) {
        animator.interpolator = timeInterpolator
    }

    override fun isRunning(): Boolean = animator.isRunning

    //endregion
}