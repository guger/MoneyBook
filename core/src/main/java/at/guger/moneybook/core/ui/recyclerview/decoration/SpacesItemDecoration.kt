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

package at.guger.moneybook.core.ui.recyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.annotation.Dimension
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * [ItemDecoration] for [RecyclerView] creating blank left surrounding the item.
 */
class SpacesItemDecoration(
    @Dimension private val all: Int = 0,
    @Dimension private val left: Int = all,
    @Dimension private val top: Int = all,
    @Dimension private val right: Int = all,
    @Dimension private val bottom: Int = all
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = if (parent.getChildAdapterPosition(view) == 0) bottom / 2 else bottom
        outRect.top = if (parent.getChildAdapterPosition(view) == 0) top / 2 else top
        outRect.right = right
        outRect.left = left
    }
}