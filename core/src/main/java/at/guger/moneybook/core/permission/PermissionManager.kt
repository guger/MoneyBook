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

package at.guger.moneybook.core.permission

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import at.guger.moneybook.core.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Utility class for managing permissions.
 */
class PermissionManager private constructor(private val fragment: WeakReference<Fragment>) {

    //region Variables

    private val requiredPermissions = mutableListOf<Permission>()
    private var checkOnly: Boolean = false

    private var rationale: RationaleInfo? = null
    private var callback: ((Boolean, Map<Permission, Boolean>) -> Unit)? = null

    private val permissionCallback = fragment.get()?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        sendResultAndClean(grantResults)
    }

    private val permissionsList: Array<String>
        get() = requiredPermissions.flatMap { it.permissions.toList() }.toTypedArray()

    //endregion

    //region Public Methods

    fun request(vararg permission: Permission, checkOnly: Boolean = false): PermissionManager {
        requiredPermissions.addAll(permission)
        this.checkOnly = checkOnly

        return this
    }

    fun rationale(info: RationaleInfo): PermissionManager {
        this.rationale = info

        return this
    }

    fun check(callback: (Boolean, Map<Permission, Boolean>) -> Unit) {
        this.callback = callback

        handlePermissionRequest()
    }

    /**
     * Requests the given permissions.
     *
     * @param permission A set of permissions
     * @param info The [RationaleInfo] to show.
     * @param callback A callback returning a boolean `isAllGranted` and the mapped request results.
     */
    fun requestPermission(
        vararg permission: Permission,
        info: RationaleInfo,
        callback: ((Boolean, Map<Permission, Boolean>) -> Unit)
    ) {
        request(*permission)
        rationale(info)

        check(callback)
    }

    fun checkPermission(
        vararg permission: Permission,
        callback: ((Boolean, Map<Permission, Boolean>) -> Unit)
    ) {
        request(*permission, checkOnly = true)

        check(callback)
    }

    //endregion

    //region Methods

    private fun handlePermissionRequest() {
        fragment.get()?.let { fragment: Fragment ->
            when {
                checkOnly -> checkPermissionsGranted(fragment)
                shouldShowPermissionRationale(fragment) -> displayRationale(fragment)
                else -> requestPermissions()
            }
        }
    }

    private fun requestPermissions() {
        permissionCallback?.launch(permissionsList)
    }

    private fun shouldShowPermissionRationale(fragment: Fragment): Boolean = rationale != null && requiredPermissions.any { it.requiresRationale(fragment) }

    private fun displayRationale(fragment: Fragment) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(rationale!!.titleRes)
            .setMessage(rationale!!.messageRes)
            .setCancelable(false)
            .setPositiveButton(R.string.Request) { _, _ -> requestPermissions() }
            .setNegativeButton(R.string.Cancel, null)
            .show()
    }

    private fun checkPermissionsGranted(fragment: Fragment) {
        sendResultAndClean(permissionsList.associate { it to hasPermission(fragment, it) })
    }

    private fun sendResultAndClean(grantResults: Map<String, Boolean>) {
        callback!!(grantResults.all { it.value }, grantResults.mapKeys { Permission.from(it.key) })

        clean()
    }

    private fun clean() {
        requiredPermissions.clear()
        rationale = null
        callback = null
    }

    //region Permission Methods

    private fun hasPermission(fragment: Fragment, permission: String) =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

    //endregion

    //endregion

    companion object {
        fun from(fragment: Fragment) = PermissionManager(WeakReference(fragment))
    }
}