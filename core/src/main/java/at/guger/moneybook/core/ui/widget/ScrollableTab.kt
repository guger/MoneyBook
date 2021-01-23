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

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.core.R
import at.guger.moneybook.core.util.Utils

/**
 * Custom TabLayout implemented with a [RecyclerView].
 */
class ScrollableTab : RecyclerView {

    //region Variables

    private val tabAdapter by lazy { TabAdapter(tabTextStyle = tabTextStyle) }
    private var selectedColor = Color.WHITE
    private var unSelectedColor = Color.GRAY

    @StyleRes
    private var tabTextStyle: Int = R.style.TabTextAppearance
    private val layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    private var viewPager: ViewPager2? = null
    private val callback: PageChangeCallback = PageChangeCallback()
    private var isRVScrolling = true
    private var listener: ((position: Int) -> Unit)? = null
    private var pageChangeListener: ((position: Int) -> Unit)? = null

    //endregion

    //region Constructor

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(set: AttributeSet?) {
        initAttributes(set)

        setLayoutManager(layoutManager)
        setHasFixedSize(true)
        adapter = tabAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(this)

        createPagerStyle()

        setOnTouchListener { _, _ ->
            isRVScrolling = true
            false
        }
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                post {
                    (0 until childCount).forEach {
                        val child = getChildAt(it)
                        val childCenterX = (child.left + child.right) / 2
                        val scaleValue = Utils.getGaussianScale(childCenterX, 1f, 1f, 150.toDouble(), left, right)
//            child.scaleX = scaleValue
//            child.scaleY = scaleValue
                        colorView(child, scaleValue)
                    }
                }
                //viewPager?.scrollBy(dx*2, 0)
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == SCROLL_STATE_IDLE) {
                    //need flat to disable this when viewpager is scrolling
                    val child = snapHelper.findSnapView(layoutManager) ?: return
                    if (isRVScrolling) viewPager?.setCurrentItem(layoutManager.getPosition(child), true)
                }
            }
        })

        tabAdapter.onTabClick {
            isRVScrolling = true
            listener?.invoke(it)
            smoothScrollToPosition(it)
            viewPager?.setCurrentItem(it, true)
        }
    }

    private fun initAttributes(set: AttributeSet?) {
        val ta = context.obtainStyledAttributes(set, R.styleable.ScrollableTab)

        selectedColor = ta.getColor(R.styleable.ScrollableTab_selectedColor, Color.WHITE)
        unSelectedColor = ta.getColor(R.styleable.ScrollableTab_unSelectedColor, Color.GRAY)
        tabTextStyle = ta.getResourceId(R.styleable.ScrollableTab_tabTextAppearance, R.style.TabTextAppearance)

        ta.recycle()
    }

    //endregion

    //region Methods

    private fun createPagerStyle() {
        //add padding and set clipToPadding false so other tab items are also visible at left,right edge screen
        clipToPadding = false
        val halfSWidth = context.resources.displayMetrics.widthPixels / 2
        val padding = halfSWidth / 2
        setPadding(padding, 0, padding, 0)
    }

    fun addTabs(list: List<String>) {
        tabAdapter.addAll(list)

        post {
            viewPager?.setCurrentItem(list.size - 1, false)
        }
    }

    fun addOnTabListener(listener: (position: Int) -> Unit) {
        this.listener = listener
    }

    fun addOnPageChangeListener(pageChangeListener: (position: Int) -> Unit) {
        this.pageChangeListener = pageChangeListener
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setUpWithViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager

        viewPager.setOnTouchListener { _, _ ->
            isRVScrolling = false
            false
        }

        viewPager.registerOnPageChangeCallback(callback)
    }

    private fun colorView(child: View, scaleValue: Float) {
        val percent = (scaleValue - 1) / 1f
        val color = ArgbEvaluator().evaluate(percent, unSelectedColor, selectedColor) as Int
        child.findViewById<TextView>(R.id.txvScrollableTabTitle).setTextColor(color)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tabAdapter.onTabClick(null)
        viewPager?.unregisterOnPageChangeCallback(callback)
        viewPager = null
    }

    //endregion

    private inner class PageChangeCallback : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            //scroll with offset divided by 2 as tab item width is half of viewpager item width)
            if (!isRVScrolling) layoutManager.scrollToPositionWithOffset(position, -positionOffsetPixels / 2)
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            smoothScrollToPosition(position)
            pageChangeListener?.invoke(position)
        }
    }
}