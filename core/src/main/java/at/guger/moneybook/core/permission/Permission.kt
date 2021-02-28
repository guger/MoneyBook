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

package at.guger.moneybook.core.permission

import android.Manifest.permission.READ_CONTACTS
import androidx.fragment.app.Fragment

/**
 * Type model for Permissions.
 */
sealed class Permission(vararg val permissions: String) {

    fun requiresRationale(fragment: Fragment) = permissions.any { fragment.shouldShowRequestPermissionRationale(it) }

    object CONTACTS : Permission(READ_CONTACTS)

    companion object {
        fun from(permission: String) = when (permission) {
            READ_CONTACTS -> CONTACTS
            else -> throw IllegalArgumentException("Unknown permission: $permission.")
        }
    }
}
