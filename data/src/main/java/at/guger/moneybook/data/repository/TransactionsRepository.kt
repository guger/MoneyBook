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

import androidx.lifecycle.LiveData
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.ContactsDao
import at.guger.moneybook.data.provider.local.dao.TransactionsDao

/**
 * Repository class for handling [transactions][Transaction].
 */
class TransactionsRepository(database: AppDatabase) {

    //region Variables

    private val transactionsDao: TransactionsDao = database.transactionsDao()
    private val contactsDao: ContactsDao = database.contactsDao()

    //endregion

    //region Methods

    suspend fun get(id: Long): Transaction = transactionsDao.get(id)

    fun getByAccount(accountId: Long) = transactionsDao.getByAccount(accountId)

    fun getEarningsAndExpenses(): LiveData<List<Transaction>> = transactionsDao.getEarningsAndExpenses()

    fun getClaimsAndDebts(): LiveData<List<Transaction>> = transactionsDao.getClaimsAndDebts()

    suspend fun insert(transaction: Transaction): Long {
        val id = transactionsDao.insert(transaction.entity)

        transaction.contacts?.let { contactsDao.insert(it) }

        return id
    }

    suspend fun update(transaction: Transaction) {
        transactionsDao.update(transaction.entity)

        val currentContacts = contactsDao.findByTransactionId(transaction.id)
        val newContacts = transaction.contacts ?: listOf()

        contactsDao.insert(newContacts - currentContacts)
        contactsDao.delete(currentContacts - newContacts)
    }

    suspend fun delete(vararg transaction: Transaction) {
        transactionsDao.delete(*transaction.map { it.entity }.toTypedArray())
    }

    //endregion
}