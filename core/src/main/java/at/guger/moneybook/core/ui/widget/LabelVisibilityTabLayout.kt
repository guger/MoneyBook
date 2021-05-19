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

package at.guger.moneybook.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.tabs.TabLayout

/**
 * [TabLayout] changing the label visibility of its child [tabs][TabLayout.Tab] depending on the selected state.
 */
class LabelVisibilityTabLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TabLayout(context, attrs, defStyleAttr) {

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        post(::updateLabelVisibility)
    }

    override fun selectTab(tab: Tab?, updateIndicator: Boolean) {
        super.selectTab(tab, updateIndicator)

        post(::updateLabelVisibility)
    }

    private fun updateLabelVisibility() {
        for (i in 0 until tabCount) {
            getTabAt(i)?.apply {
                tabLabelVisibility = if (isSelected) TAB_LABEL_VISIBILITY_LABELED else TAB_LABEL_VISIBILITY_UNLABELED
            }
        }
    }
}