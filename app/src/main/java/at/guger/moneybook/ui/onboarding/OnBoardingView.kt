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
import at.guger.moneybook.databinding.ViewOnboardingBinding
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator

/**
 * [FrameLayout] containing the OnBoarding View.
 */
class OnBoardingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    //region Variables

    private val binding: ViewOnboardingBinding = ViewOnboardingBinding.inflate(LayoutInflater.from(context), this, true)

    private val numberOfPages by lazy { OnBoardingPage.values().size }

    var skipButtonCallback: (() -> Unit)? = null
    var startMigrateButtonCallback: (() -> Unit)? = null

    //endregion

    init {
        setupSlider(binding.root)
        addButtonClickListeners()
    }

    //region Methods

    private fun setupSlider(view: View) {
        with(binding.mOnBoardingViewSlider) {
            adapter = OnBoardingPagerAdapter(OnBoardingPage.values())

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    if (numberOfPages > 1) {
                        binding.mOnBoardingViewRoot.progress = (position + positionOffset) / (numberOfPages - 1)
                    }
                }
            })

            val indicator = view.findViewById<WormDotsIndicator>(R.id.mOnBoardingViewIndicator)
            indicator.setViewPager2(this)
        }
    }

    private fun addButtonClickListeners() {
        binding.btnOnBoardingViewNext.setOnClickListener {
            val nextSlide = binding.mOnBoardingViewSlider.currentItem.plus(1)
            binding.mOnBoardingViewSlider.setCurrentItem(nextSlide, true)
        }
        binding.btnOnBoardingViewSkip.setOnClickListener { skipButtonCallback?.invoke() }
        binding.btnOnBoardingViewStartMigrate.setOnClickListener { startMigrateButtonCallback?.invoke() }
    }

    //endregion
}