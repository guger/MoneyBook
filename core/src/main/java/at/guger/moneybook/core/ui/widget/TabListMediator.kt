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

package at.guger.moneybook.core.ui.widget

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.core.util.ext.doIf
import com.google.android.material.tabs.TabLayout

/**
 * Mediator for syncing a [RecyclerView] list with a [TabLayout].
 *
 * @param tabLayout The [TabLayout] which will be attached to the [RecyclerView].
 * @param recyclerView The [RecyclerView] holding the item list.
 * @param itemComparator The comparator providing whether two items will get an equal title/tab.
 * @param itemTitleProvider A provider returning the title for a given item index.
 */
class TabListMediator(
    private val tabLayout: TabLayout,
    private val recyclerView: RecyclerView,
    private val itemComparator: (Int, Int) -> Boolean,
    private val itemTitleProvider: (Int) -> String,
    private val onTabChangedCallback: ((Int, IntRange) -> Unit)? = null
) {

    //region Variables

    private var init: Boolean = false

    private var currentTitleItemPosition: Int = -1

    private val tabs = mutableMapOf<IntRange, String>()

    private var state: Int = STATE_IDLE

    private val layoutManager: LinearLayoutManager

    //endregion

    init {
        require(recyclerView.layoutManager is LinearLayoutManager) { "TabListMediator does only support LinearLayoutManager." }
        layoutManager = recyclerView.layoutManager as LinearLayoutManager
    }

    //region Methods

    /**
     * Attaches the [TabLayout] to the given [RecyclerView].
     *
     * This method must not be called before the [RecyclerView.Adapter] has committed the changes internally.
     * Otherwise, the tabs will not be correctly in sync with the RecyclerView's items.
     */
    fun attach() {
        if (!init) setupObservers()

        onItemsUpdated()

        tabLayout.removeAllTabs()
        tabs.values.reversed().map(::createTab).forEachIndexed { index, tab -> tabLayout.addTab(tab, index == tabs.size - 1) }
        state = STATE_IDLE
    }

    private fun setupObservers() {
        init = true

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                invalidate()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) state = STATE_IDLE
            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val currentItem = tabs.toList().single { it.second == tab!!.text }

                if (state != STATE_RECYCLER_VIEW_SCROLLING) {
                    state = STATE_TAB_SELECTED

                    currentTitleItemPosition = currentItem.first.first
                    recyclerView.smoothScrollToPosition(currentTitleItemPosition)
                }

                onTabChangedCallback?.invoke(tab!!.position, currentItem.first)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun onItemsUpdated() {
        tabs.clear()

        if (layoutManager.itemCount == 0) return

        val indicesList = mutableListOf(0)

        for (i in 1 until layoutManager.itemCount) {
            if (!itemComparator(i - 1, i)) {
                indicesList.add(i)
            }
        }

        indicesList.add(layoutManager.itemCount)

        for (i in 1 until indicesList.size) {
            val range = IntRange(indicesList[i - 1], indicesList[i] - 1)

            tabs[range] = itemTitleProvider(indicesList[i - 1])
        }

        updateTabLayout(tabs.size - 1)
    }

    private fun invalidate() {
        if (state == STATE_TAB_SELECTED) return

        val viewItemPosition = when {
            layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1 -> layoutManager.findLastCompletelyVisibleItemPosition()
            layoutManager.findFirstVisibleItemPosition() == 0 -> layoutManager.findFirstVisibleItemPosition()
            else -> getMidVisibleRecyclerItemPosition()
        }

        if (viewItemPosition >= 0) {
            if (!itemComparator(currentTitleItemPosition, viewItemPosition)) {
                currentTitleItemPosition = viewItemPosition

                val tabIndex = tabs.keys.reversed().withIndex().find { it.value.contains(currentTitleItemPosition) }!!.index
                updateTabLayout(tabIndex)
            }
        }
    }

    private fun getMidVisibleRecyclerItemPosition(): Int {
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        return (firstVisibleItemPosition + lastVisibleItemPosition) / 2
    }

    private fun updateTabLayout(tabPosition: Int) {
        state = STATE_RECYCLER_VIEW_SCROLLING

        tabLayout.getTabAt(tabPosition)?.doIf({ !it.isSelected }) { select() }

        state = STATE_IDLE
    }

    private fun createTab(title: String): TabLayout.Tab {
        return tabLayout.newTab().apply {
            text = title
        }
    }

    //endregion

    companion object {
        private const val STATE_IDLE = 0
        private const val STATE_RECYCLER_VIEW_SCROLLING = 1
        private const val STATE_TAB_SELECTED = 2
    }
}