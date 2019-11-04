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

package at.guger.moneybook.data.repository

import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.ContactsDao

/**
 * Repository class for handling [contacts][Contact].
 */
class ContactsRepository(database: AppDatabase, private val addressBookRepository: AddressBookRepository) {

    //region Variables

    private val contactsDao: ContactsDao = database.contactsDao()

    //endregion

    //region Methods

    suspend fun findByTransactionId(transactionId: Long): List<Contact> {
        val unnamedContacts = contactsDao.findByTransactionId(transactionId)

        val contacts = mutableListOf<Contact>()

        val addressBookContacts = addressBookRepository.loadContacts(unnamedContacts.filter { it.contactId >= 0 }.map { it.contactId }.toLongArray())

        addressBookContacts.forEach { unnamedContacts.find { contact -> contact.contactId == it.key }?.copy(contactName = it.value)?.let(contacts::add) }

        return contacts
    }

    suspend fun insert(contacts: List<Contact>) {
        contactsDao.insert(contacts)
    }

    suspend fun update(contacts: List<Contact>) {
        contactsDao.update(contacts)
    }

    suspend fun delete(contacts: List<Contact>) {
        contactsDao.delete(contacts)
    }

    //endregion
}