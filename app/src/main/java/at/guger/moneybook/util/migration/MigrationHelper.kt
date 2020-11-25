/*
 * Copyright 2020 Daniel Guger
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

package at.guger.moneybook.util.migration

import android.Manifest
import android.content.Context
import at.guger.moneybook.core.util.ext.hasPermission
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.provider.legacy.LegacyAppDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.model.BookEntry
import at.guger.moneybook.data.provider.legacy.model.Category
import at.guger.moneybook.data.provider.legacy.reminder.ReminderManager
import at.guger.moneybook.data.repository.AddressBookRepository
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.scheduler.reminder.ReminderScheduler
import java.time.LocalDateTime
import kotlin.math.max

/**
 * Migration helper for converting v1 data to v2.
 *
 * This package will be deleted when all users moved to v2.
 */
class MigrationHelper(
    private val context: Context,
    private val transactionsRepository: TransactionsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val addressBookRepository: AddressBookRepository,
    private val reminderScheduler: ReminderScheduler
) {

    //region Variables

    private val legacyDatabase: LegacyAppDatabase = LegacyAppDatabase.get(context)

    private var entries: List<BookEntry>? = null

    var categories = emptyList<Category>()
    lateinit var account: Account

    //endregion

    //region Methods

    /**
     * Loads all used categories from the legacy database.
     *
     * @return The used categories, or null, if there are no book entries at all.
     */
    suspend fun getUsedCategories(): List<Category>? {
        entries = legacyDatabase.bookEntryDao().getBookEntries()

        return legacyDatabase.categoryDao().getCategories().filter {
            legacyDatabase.categoryDao().countCategoryUsage(it.id) > 0 && entries!!.any { entry -> entry.category?.id == it.id && entry.entryType == Transaction.TransactionType.EXPENSE }
        }.takeIf { entries!!.isNotEmpty() }
    }

    suspend fun migrate() {
        checkNotNull(entries) { "Call #getUsedCategories before migration." }

        val legacyReminders: Map<Long, LocalDateTime> = legacyDatabase.reminderDao().getReminders().map { Pair(it.bookEntryId, it.fireDate) }.toMap()
        val budgets = mutableMapOf<Long, Budget>()

        categories.forEach {
            val budget = Budget(
                name = it.name,
                budget = computeBudget(it, entries!!),
                color = it.color
            )

            budget.id = budgetsRepository.insert(budget)

            budgets[it.id] = budget
        }

        entries!!.forEach { entry ->
            val reminder = legacyReminders[entry.id]?.takeIf { it.isAfter(LocalDateTime.now()) }

            val transaction = Transaction.TransactionEntity(
                title = entry.title,
                date = entry.date,
                value = entry.value,
                due = reminder?.toLocalDate(),
                notes = entry.notes,
                type = entry.entryType,
                accountId = account.id.takeIf { entry.entryType == Transaction.TransactionType.EARNING || entry.entryType == Transaction.TransactionType.EXPENSE },
                budgetId = budgets[entry.category?.id ?: -1]?.id?.takeIf { entry.entryType == Transaction.TransactionType.EXPENSE }
            )

            var contacts: List<Contact>? = null
            if (context.hasPermission(Manifest.permission.READ_CONTACTS) && entry.embeddedContacts != null) {
                val contactNames = addressBookRepository.loadContacts(entry.embeddedContacts!!.map { it.contactId }.toSet())

                contacts = entry.embeddedContacts!!.map { entity ->
                    Contact(
                        contactId = entity.contactId,
                        contactName = contactNames.getValue(entity.contactId),
                        paidState = if (entity.hasPaid) Contact.PaidState.STATE_PAID else Contact.PaidState.STATE_NOT_PAID
                    )
                }
            }

            val transactionId = transactionsRepository.insert(
                Transaction(
                    entity = transaction,
                    contacts = contacts
                )
            )

            reminder?.let { dateTime ->
                ReminderManager.cancelReminder(context, entry.id)
                reminderScheduler.scheduleReminder(transactionId, dateTime.toLocalDate())
            }
        }

        finishMigration()
    }

    fun finishMigration() {
        context.deleteDatabase(LegacyDatabase.DATABASE_NAME)
    }

    private fun computeBudget(category: Category, entries: List<BookEntry>): Double {
        val relevantEntries = entries.filter { it.category?.id == category.id }
        val sum: Double = relevantEntries.sumByDouble { it.value }

        val firstDate = relevantEntries.minByOrNull { it.date }!!.date.toEpochDay()
        val lastDate = relevantEntries.maxByOrNull { it.date }!!.date.toEpochDay()

        val months: Double = (lastDate - firstDate) / 30.0

        return roundUpTo(sum / max(months, 1.0), 25)
    }

    fun roundUpTo(x: Double, n: Int): Double {
        return (((x + if (x % n > 0.001) n.toDouble() else n / 2.0) / n).toLong() * n).toDouble()
    }

    //endregion

    companion object {
        fun needMigration(context: Context) = context.databaseList().contains(LegacyDatabase.DATABASE_NAME)
    }
}