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

package at.guger.moneybook.core.util.permissions

import android.app.Activity
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.ConfirmCallback
import com.afollestad.assent.rationale.RationaleHandler
import com.afollestad.assent.rationale.Requester
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Material alert dialog rationale handler for assent.
 */
class MaterialAlertDialogRationale(
    private val activity: Activity,
    @StringRes private val dialogTitle: Int,
    requester: Requester,
    block: RationaleHandler.() -> Unit
) : RationaleHandler(activity, requester) {

    init {
        block()
    }

    private var dialog: AlertDialog? = null

    override fun showRationale(
        permission: Permission,
        message: CharSequence,
        confirm: ConfirmCallback
    ) {
        dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(dialogTitle)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                (dialog as AlertDialog).setOnDismissListener(null)
                confirm(true)
            }
            .setOnDismissListener {
                confirm(false)
            }
            .show()
    }

    override fun onDestroy() {
        dialog?.dismiss()
        dialog = null
    }
}