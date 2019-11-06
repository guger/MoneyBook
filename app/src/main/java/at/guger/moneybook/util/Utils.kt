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

package at.guger.moneybook.util

import android.app.Activity
import at.guger.moneybook.R
import com.afollestad.assent.Permission
import com.afollestad.assent.rationale.createDialogRationale

/**
 * Common methods and fields.
 */
object Utils {

    fun createContactsPermissionRationale(activity: Activity) = activity.createDialogRationale(R.string.ContactsPermission) {
        onPermission(Permission.READ_CONTACTS, R.string.ContactsPermissionNeeded)
    }
}