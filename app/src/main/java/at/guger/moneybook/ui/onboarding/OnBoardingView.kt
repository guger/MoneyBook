/*
 *
 *  * Copyright 2020 Daniel Guger
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package at.guger.moneybook.ui.onboarding

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.R
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import kotlinx.android.synthetic.main.item_onboarding_page.view.*
import kotlinx.android.synthetic.main.view_onboarding.view.*

/**
 * [FrameLayout] containing the OnBoarding View.
 */
class OnBoardingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val numberOfPages by lazy { OnBoardingPage.values().size }

    var skipButtonCallback: (() -> Unit)? = null
    var startMigrateButtonCallback: (() -> Unit)? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_onboarding, this, true)
        setupSlider(view)
        addButtonClickListeners()
    }

    //region Methods

    private fun setupSlider(view: View) {
        with(mOnBoardingViewSlider) {
            adapter = OnBoardingPagerAdapter(OnBoardingPage.values())

            setPageTransformer { page, position -> setParallaxTransformation(page, position) }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    if (numberOfPages > 1) {
                        this@OnBoardingView.mOnBoardingViewRoot.progress = (position + positionOffset) / (numberOfPages - 1)
                    }
                }
            })

            val indicator = view.findViewById<WormDotsIndicator>(R.id.mOnBoardingViewIndicator)
            indicator.setViewPager2(this)
        }
    }

    private fun addButtonClickListeners() {
        btnOnBoardingViewNext.setOnClickListener {
            val nextSlide = mOnBoardingViewSlider.currentItem.plus(1)
            mOnBoardingViewSlider.setCurrentItem(nextSlide, true)
        }
        btnOnBoardingViewSkip.setOnClickListener { skipButtonCallback?.invoke() }
        btnOnBoardingViewStartMigrate.setOnClickListener { startMigrateButtonCallback?.invoke() }
    }

    private fun setParallaxTransformation(page: View, position: Float) {
        page.apply {
            when {
                position < -1 -> alpha = 1.0f // (-Inf, -1) -> way off screen to the left
                position <= 1 -> imvOnBoardingItem.translationX = -position * (width / 2) // [-1, 1] -> half the normal speed
                else -> alpha = 1.0f // (1, Inf) -> way off screen to the right
            }
        }
    }

    //endregion
}