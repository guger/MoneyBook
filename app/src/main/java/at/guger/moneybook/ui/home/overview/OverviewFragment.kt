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

package at.guger.moneybook.ui.home.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseViewBindingFragment
import at.guger.moneybook.core.ui.recyclerview.decoration.SpacesItemDecoration
import at.guger.moneybook.core.util.ext.dimen
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.databinding.FragmentOverviewBinding
import at.guger.moneybook.ui.home.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] overview content.
 */
class OverviewFragment : BaseViewBindingFragment<FragmentOverviewBinding>() {

    //region Variables

    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): FragmentOverviewBinding {
        return FragmentOverviewBinding.inflate(inflater, parent, attachToParent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val animSet = AnimationSet(true).apply {
            interpolator = DecelerateInterpolator()

            addAnimation(
                AlphaAnimation(0.0f, 1.0f).apply {
                    duration = 600
                }
            )
            addAnimation(
                TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.25f, Animation.RELATIVE_TO_SELF, 0.0f
                ).apply {
                    duration = 450
                }
            )
        }

        binding.mOverviewRecyclerView.setup(LinearLayoutManager(requireContext()), OverviewAdapter(viewModel, viewLifecycleOwner)) {
            addItemDecoration(SpacesItemDecoration(context.dimen(res = R.dimen.recyclerview_item_spacing).toInt()))

            layoutAnimation = LayoutAnimationController(animSet, 0.4f)
        }
    }

    //endregion

    //region Methods

    //endregion

    companion object {
        fun instantiate(): OverviewFragment = OverviewFragment()
    }
}