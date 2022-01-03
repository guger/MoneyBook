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

package at.guger.moneybook.core.ui.recyclerview.listener

import android.content.Context
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

/**
 * Touch listener for [RecyclerView].
 */
class OnItemTouchListener(context: Context, recyclerView: RecyclerView, private val onTouchCallback: ItemTouchListener) : RecyclerView.OnItemTouchListener {

    //region Variables

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        private val MIN_SWIPE_DISTANCE: Int = 50
        private lateinit var downMotionEvent: MotionEvent

        override fun onDown(e: MotionEvent?): Boolean {
            e?.let { downMotionEvent = it }

            return super.onDown(e)
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            e?.let {
                val child: View? = recyclerView.findChildViewUnder(it.x, it.y)

                if (child != null && !isGestureSwipe(it)) {
                    onTouchCallback.onItemLongClick(child, recyclerView.getChildLayoutPosition(child), it)
                }
            }

            super.onLongPress(e)
        }

        fun isGestureSwipe(e: MotionEvent): Boolean {
            return downMotionEvent.x - e.x >= MIN_SWIPE_DISTANCE
        }
    })

    //endregion

    //region TouchHandler

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val childView = rv.findChildViewUnder(e.x, e.y)

        if (childView != null && gestureDetector.onTouchEvent(e)) {
            onTouchCallback.onItemClick(childView, rv.getChildLayoutPosition(childView), e)
        }

        return false
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    //endregion

    interface ItemTouchListener {
        fun onItemClick(view: View, pos: Int, e: MotionEvent)
        fun onItemLongClick(view: View, pos: Int, e: MotionEvent)
    }

    companion object {

        fun isViewClicked(container: View, @IdRes viewId: Int, e: MotionEvent): Boolean {
            val view = container.findViewById<View>(viewId)

            return isViewClicked(view, e)
        }

        fun isViewClicked(view: View, e: MotionEvent): Boolean {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)

            return view.isVisible && rect.contains(e.rawX.toInt(), e.rawY.toInt())
        }
    }
}