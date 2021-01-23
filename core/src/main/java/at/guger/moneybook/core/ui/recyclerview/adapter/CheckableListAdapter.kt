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

package at.guger.moneybook.core.ui.recyclerview.adapter

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.guger.moneybook.data.model.base.Model

/**
 * Interface for a [RecyclerView.Adapter] supporting checked items.
 */
abstract class CheckableListAdapter<T : Model, VH : RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>) : ListAdapter<T, VH>(diffCallback) {

    abstract val checkedItems: MutableList<Int>

    val checkedCount: Int
        get() = checkedItems.count()

    @CallSuper
    open fun toggleChecked(pos: Int) {
        if (!checkedItems.contains(pos)) checkedItems.add(pos) else checkedItems.remove(pos)
    }

    @CallSuper
    open fun clearChecked() {
        checkedItems.clear()
    }
}