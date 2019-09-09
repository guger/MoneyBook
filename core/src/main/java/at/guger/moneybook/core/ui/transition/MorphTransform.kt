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

package at.guger.moneybook.core.ui.transition

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import at.guger.moneybook.core.R

/**
 * An extension to [ChangeBounds] that also morphs the views background (color & corner radius).
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MorphTransform(context: Context, attrs: AttributeSet) : ChangeBounds(context, attrs) {

    //region Variables

    private val startColor: Int
    private val endColor: Int
    private val startCornerRadius: Float
    private val endCornerRadius: Float

    //endregion

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MorphTransform)

        startColor = a.getColor(R.styleable.MorphTransform_startColor, Color.BLACK)
        endColor = a.getColor(R.styleable.MorphTransform_endColor, Color.WHITE)
        startCornerRadius = a.getDimension(R.styleable.MorphTransform_startCornerRadius, 0f)
        endCornerRadius = a.getDimension(R.styleable.MorphTransform_endCornerRadius, 0f)

        a.recycle()

        pathMotion = GravityArcMotion()
    }

    override fun createAnimator(sceneRoot: ViewGroup?, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        val changeBounds = super.createAnimator(sceneRoot, startValues, endValues) ?: return null

        val interpolator = FastOutSlowInInterpolator()

        val background = MorphDrawable(startColor, startCornerRadius)
        endValues!!.view.background = background

        val colorAnimator = ObjectAnimator.ofArgb(background, MorphDrawable.COLOR, endColor)
        val cornerAnimator = ObjectAnimator.ofFloat(background, MorphDrawable.CORNERRADIUS, endCornerRadius)

        // Ease in the child views (fade in & staggered slide up)
        if (endValues is ViewGroup) {
            val viewGroup = endValues.view as ViewGroup

            val easeDuration = duration / 2
            var offset = viewGroup.height / 3f

            for (view in viewGroup.children) {
                with(view) {
                    translationY = offset
                    alpha = 0f
                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(easeDuration)
                        .setStartDelay(easeDuration)
                        .setInterpolator(interpolator)
                }

                offset *= 1.8f
            }
        }

        return AnimatorSet().apply {
            playTogether(changeBounds, cornerAnimator, colorAnimator)
            duration = this@MorphTransform.duration
            setInterpolator(interpolator)
        }
    }
}