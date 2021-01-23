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

package at.guger.moneybook.core.util.ext

import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Extension functions for [FloatingActionButton].
 */

fun FloatingActionButton.showIfHidden() {
    if (visibility != View.VISIBLE) show()
}

fun FloatingActionButton.hideIfShown() {
    if (visibility == View.VISIBLE) hide()
}