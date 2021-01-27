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

package at.guger.moneybook.work

import android.Manifest
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.guger.moneybook.core.util.ext.hasPermission
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.repository.AddressBookRepository
import at.guger.moneybook.data.repository.ContactsRepository

/**
 * [CoroutineWorker] for syncing contacts with the address book.
 */
class ContactsSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val contactsRepository: ContactsRepository,
    private val addressBookRepository: AddressBookRepository
) : CoroutineWorker(context, params) {

    //region Methods

    //endregion

    override suspend fun doWork(): Result {
        if (applicationContext.hasPermission(Manifest.permission.READ_CONTACTS)) {
            val contacts = contactsRepository.getContacts()

            val contactsToUpdate = mutableListOf<Contact>()

            val addressBook = addressBookRepository.loadContacts(contacts.filter { it.contactId >= 0 }.map { it.contactId }.toSet())

            contacts.forEach { contact ->
                val addressBookName = addressBook[contact.contactId]
                if (addressBookName != null && addressBookName != contact.contactName) contactsToUpdate.add(contact.copy(contactName = addressBookName))
            }

            return Result.success()
        } else {
            return Result.failure()
        }
    }
}