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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.recyclerview.viewholder.ContainerViewHolder
import kotlinx.android.synthetic.main.item_onboarding_page.*

/**
 * [RecyclerView.Adapter] for the OnBoarding ViewPager.
 */
class OnBoardingPagerAdapter(private val onBoardingPages: Array<OnBoardingPage>) : RecyclerView.Adapter<OnBoardingPagerAdapter.OnBoardingPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnBoardingPagerViewHolder {
        return OnBoardingPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false))
    }

    override fun onBindViewHolder(holder: OnBoardingPagerViewHolder, position: Int) {
        holder.bind(onBoardingPages[position])
    }

    override fun getItemCount(): Int = onBoardingPages.size

    class OnBoardingPagerViewHolder(override val containerView: View?) : ContainerViewHolder(containerView) {

        fun bind(page: OnBoardingPage) {
            txvOnBoardingItemTitle.setText(page.title)
            txvOnBoardingItemSubtitle.setText(page.subtitle)
            txvOnBoardingItemDescription.setText(page.description)
            imvOnBoardingItem.setImageResource(page.image)
        }

        override fun clear() {}
    }
}