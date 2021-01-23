/*
 *
 *  * Copyright 2021 Daniel Guger
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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.databinding.ItemOnboardingPageBinding

/**
 * [RecyclerView.Adapter] for the OnBoarding ViewPager.
 */
class OnBoardingPagerAdapter(private val onBoardingPages: Array<OnBoardingPage>) : RecyclerView.Adapter<OnBoardingPagerAdapter.OnBoardingPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingPagerViewHolder {
        return OnBoardingPagerViewHolder(ItemOnboardingPageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: OnBoardingPagerViewHolder, position: Int) {
        holder.bind(onBoardingPages[position])
    }

    override fun getItemCount(): Int = onBoardingPages.size

    class OnBoardingPagerViewHolder(val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnBoardingPage) {
            with(binding) {
                txvOnBoardingItemTitle.setText(page.title)
                txvOnBoardingItemSubtitle.setText(page.subtitle)
                txvOnBoardingItemDescription.setText(page.description)
                imvOnBoardingItem.setImageResource(page.image)
            }
        }
    }
}