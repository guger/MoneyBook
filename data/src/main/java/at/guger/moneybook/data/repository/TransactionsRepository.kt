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

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.model.TransactionSuggestion
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.ContactsDao
import at.guger.moneybook.data.provider.local.dao.TransactionsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import java.time.LocalDate

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

    fun getByAccount(accountId: Long): LiveData<List<Transaction>> = transactionsDao.getByAccount(accountId)

    fun getByAccountMonthly(accountId: Long): LiveData<List<LocalDate>> = getByAccount(accountId).map { transactions ->
        transactions.map { transaction -> transaction.date }.groupBy { date -> date.withDayOfMonth(1) }.keys.reversed().takeIf { it.isNotEmpty() } ?: listOf(
            LocalDate.now().withDayOfMonth(1)
        )
    }

    fun getByAccountMonth(accountId: Long, month: LocalDate): LiveData<List<Transaction>> = getByAccount(accountId).map {
        it.filter { transaction -> transaction.date.withDayOfMonth(1).isEqual(month) }
    }

    fun getByBudget(budgetId: Long): LiveData<List<Transaction>> = transactionsDao.getByBudget(budgetId)

    fun getByBudgetMonthly(budgetId: Long): LiveData<List<LocalDate>> = getByBudget(budgetId).map {
        it.map { transaction -> transaction.date }.groupBy { date -> date.withDayOfMonth(1) }.keys.reversed()
    }

    fun getByBudgetMonth(budgetId: Long, month: LocalDate): LiveData<List<Transaction>> = getByBudget(budgetId).map {
        it.filter { transaction -> transaction.date.withDayOfMonth(1).isEqual(month) }
    }

    fun getEarningsAndExpenses(): LiveData<List<Transaction>> = transactionsDao.getEarningsAndExpenses()

    fun getClaimsAndDebts(): LiveData<List<Transaction>> = transactionsDao.getClaimsAndDebts()

    suspend fun insert(transaction: Transaction): Long {
        val id = transactionsDao.insert(transaction.entity)

        transaction.contacts?.let {
            it.forEach { contact -> contact.transactionId = id }

            contactsDao.insert(it)
        }

        return id
    }

    suspend fun update(transaction: Transaction) {
        transactionsDao.update(transaction.entity)

        val currentContacts = contactsDao.findByTransactionId(transaction.id)
        val newContacts = transaction.contacts ?: listOf()

        newContacts.forEach { contact -> contact.transactionId = transaction.id }

        val contactsToDelete = currentContacts.filterNot { newContacts.contains(it) }
        val contactsToInsert = newContacts.filterNot { currentContacts.contains(it) }

        contactsDao.delete(contactsToDelete)
        contactsDao.insert(contactsToInsert)
    }

    suspend fun delete(vararg transaction: Transaction) {
        transactionsDao.delete(*transaction.map { it.entity }.toTypedArray())
    }

    suspend fun markAsPaid(transactionId: Long) {
        markAsPaid(get(transactionId))
    }

    suspend fun markAsPaid(transaction: Transaction) {
        update(
            when (transaction.type) {
                Transaction.TransactionType.EARNING, Transaction.TransactionType.EXPENSE -> throw IllegalArgumentException("Earnings and expenses must not be marked as paid.")
                else -> Transaction(
                    entity = transaction.entity.copy(isPaid = true),
                    contacts = transaction.contacts?.onEach { it.paidState = Contact.PaidState.STATE_PAID }
                )
            }
        )
    }

    suspend fun moveToAccount(transaction: Transaction, targetAccountId: Long) {
        update(
            when (transaction.type) {
                Transaction.TransactionType.EARNING, Transaction.TransactionType.EXPENSE -> throw IllegalArgumentException("Earnings and expenses must not be marked as paid.")
                else -> Transaction(
                    entity = transaction.entity.copy(
                        isPaid = true,
                        date = LocalDate.now(),
                        accountId = targetAccountId,
                        type = if (transaction.type == Transaction.TransactionType.CLAIM) Transaction.TransactionType.EARNING else Transaction.TransactionType.EXPENSE
                    ),
                    contacts = transaction.contacts?.onEach { it.paidState = Contact.PaidState.STATE_PAID }
                )
            }
        )
    }

    fun findTransactions(query: String, count: Int = 5): Flow<List<TransactionSuggestion>> {
        return transactionsDao.findTransactions(query).transform { l ->
            emit(l.distinctBy { it.value }.take(count))
        }
    }

    //endregion
}