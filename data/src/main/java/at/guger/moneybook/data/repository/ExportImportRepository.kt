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

import at.guger.moneybook.data.model.*
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.*

/**
 * Repository class for performing an export or import.
 */
class ExportImportRepository(database: AppDatabase) {

    //region Variables

    private val transactionsDao: TransactionsDao = database.transactionsDao()
    private val accountsDao: AccountsDao = database.accountsDao()
    private val contactsDao: ContactsDao = database.contactsDao()
    private val reminderDao: ReminderDao = database.reminderDao()
    private val budgetsDao: BudgetsDao = database.budgetsDao()

    //endregion

    //region Methods

    suspend fun getTransactionEntities(): List<Transaction.TransactionEntity> = transactionsDao.getEntities()

    suspend fun insertTransactions(transactionEntities: List<Transaction.TransactionEntity>) {
        transactionEntities.forEach { transactionsDao.insert(it) }
    }

    suspend fun getAccounts(): List<Account> {
        return accountsDao.getAccounts()
    }

    suspend fun insertAccounts(accounts: List<Account>) {
        accountsDao.insert(*accounts.toTypedArray())
    }

    suspend fun getContacts(): List<Contact> = contactsDao.getContacts()

    suspend fun insertContacts(contacts: List<Contact>) {
        contactsDao.insert(contacts)
    }

    suspend fun getReminders(): List<Reminder> {
        return reminderDao.getReminders()
    }

    suspend fun getBudgets(): List<Budget> = budgetsDao.getBudgets()

    suspend fun insertBudgets(budgets: List<Budget>) {
        budgetsDao.insert(*budgets.toTypedArray())
    }

    suspend fun deleteAll() {
        transactionsDao.deleteAll()
        accountsDao.deleteAll()
        budgetsDao.deleteAll()
        contactsDao.deleteAll()
        reminderDao.deleteAll()
    }
    //endregion
}