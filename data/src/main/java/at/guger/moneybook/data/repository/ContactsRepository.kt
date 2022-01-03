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

import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.ContactsDao

/**
 * Repository class for handling [contacts][Contact].
 */
class ContactsRepository(database: AppDatabase) {

    //region Variables

    private val contactsDao: ContactsDao = database.contactsDao()

    //endregion

    //region Methods

    suspend fun findByTransactionId(transactionId: Long): List<Contact> = contactsDao.findByTransactionId(transactionId)

    suspend fun getContacts(): List<Contact> = contactsDao.getContacts()

    suspend fun update(contacts: List<Contact>) = contactsDao.update(contacts)

    //endregion
}