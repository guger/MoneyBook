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

package at.guger.moneybook.data.repository

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import at.guger.moneybook.data.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class for handling [address book contacts][Contact].
 */
class AddressBookRepository(private val contentResolver: ContentResolver) {

    //region Variables

    //endregion

    //region Methods

    suspend fun loadContacts(ids: Set<Long>? = null): Map<Long, String> = withContext(Dispatchers.IO) {
        val selection: String? = ids?.run { CONTACT_ID }
        val args: Array<String>? = ids?.toList()?.let { contactIds ->
            Array(contactIds.size) { i -> contactIds[i].toString() }
        }

        val contacts = mutableMapOf<Long, String>()

        makeContactsCursor(selection, args)?.run {
            val idCol = getColumnIndex(CONTACT_ID)
            val nameCol = getColumnIndex(DISPLAY_NAME)

            if (moveToFirst()) {
                do {
                    val id = getLong(idCol)
                    val displayName = getString(nameCol)

                    if (displayName != null) contacts[id] = displayName
                } while (moveToNext())
            }

            close()
        }

        return@withContext contacts
    }

    suspend fun loadPhoneNumbers(ids: Set<Long>): Map<Long, String> = withContext(Dispatchers.IO) {
        val phoneNumbers = mutableMapOf<Long, String>()
        val args: Array<String> = Array(ids.size) { i -> ids.toList()[i].toString() }

        makePhoneNumberCursor(args)?.run {
            val idCol = getColumnIndex(PHONE_CONTACT_ID)
            val numberCol = getColumnIndex(PHONE_NUMBER_NORMALIZED)
            val numberTypeCol = getColumnIndex(PHONE_NUMBER_TYPE)

            if (moveToFirst()) {
                do {
                    if (getInt(numberTypeCol) == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                        phoneNumbers[getLong(idCol)] = getString(numberCol)
                    }
                } while (moveToNext())
            }

            close()
        }

        return@withContext phoneNumbers
    }

    //endregion

    //region Cursor Methods

    private fun makeContactsCursor(selection: String?, args: Array<String>?): Cursor? {
        var contactsSelection: String? = null

        if (selection != null) {
            contactsSelection = "$selection IN(" + Array(args!!.size) { "?" }.joinToString() + ")"
        }

        return contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, arrayOf(
                CONTACT_ID,
                DISPLAY_NAME
            ), contactsSelection, args, "$DISPLAY_NAME ASC"
        )
    }

    private fun makePhoneNumberCursor(args: Array<String>): Cursor? {
        val selection = "$PHONE_CONTACT_ID IN(" + Array(args.size) { "?" }.joinToString() + ")"
        return contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(PHONE_CONTACT_ID, PHONE_NUMBER_NORMALIZED, PHONE_NUMBER_TYPE), selection, args, null)
    }

    //endregion

    companion object {
        const val CONTACT_ID = ContactsContract.Contacts._ID
        const val DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME
        const val PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        const val PHONE_NUMBER_NORMALIZED = ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
        const val PHONE_NUMBER_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE
    }
}