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

package at.guger.moneybook.core.ui.fragment

import androidx.fragment.app.DialogFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Main dialog fragment class.
 */
abstract class BaseDialogFragment : DialogFragment() {

    protected val disposables by lazy { CompositeDisposable() }

    //region Fragment

    override fun onPause() {
        super.onPause()

        disposables.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        disposables.dispose()
    }

    //endregion
}