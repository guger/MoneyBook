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

package at.guger.moneybook.core.ui.recyclerview.viewholder

import androidx.databinding.ViewDataBinding
import at.guger.moneybook.data.model.base.Model

/**
 * Base view holder for items using data binding and a model.
 */
abstract class ModelViewHolder<T : ViewDataBinding, M : Model>(protected val binding: T) : ContainerViewHolder(binding.root) {

    abstract fun bind(model: M)
}