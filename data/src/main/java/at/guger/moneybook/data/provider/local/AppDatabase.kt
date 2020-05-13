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

package at.guger.moneybook.data.provider.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import androidx.work.WorkRequest
import at.guger.moneybook.data.Database.DATABASE_NAME
import at.guger.moneybook.data.Database.DATABASE_VERSION
import at.guger.moneybook.data.model.*
import at.guger.moneybook.data.provider.local.converter.DateConverter
import at.guger.moneybook.data.provider.local.converter.RecurrenceConverter
import at.guger.moneybook.data.provider.local.dao.*

/**
 * Local database for transactions, budgets, contacts and reminders.
 */
@Database(
    entities = [Transaction.TransactionEntity::class, Account::class, Budget::class, Contact::class, Reminder::class],
    version = DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(DateConverter::class, RecurrenceConverter::class)
abstract class AppDatabase : RoomDatabase() {

    internal abstract fun transactionsDao(): TransactionsDao
    internal abstract fun accountsDao(): AccountsDao
    internal abstract fun budgetsDao(): BudgetsDao
    internal abstract fun contactsDao(): ContactsDao
    internal abstract fun reminderDao(): ReminderDao

    companion object {

        fun get(context: Context, onCreateWorkers: List<WorkRequest>? = null, onOpenWorkers: List<WorkRequest>? = null): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        onCreateWorkers?.let { WorkManager.getInstance(context).enqueue(it) }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)

                        onOpenWorkers?.let { WorkManager.getInstance(context).enqueue(it) }
                    }
                })
                .addMigrations()
                .build()
        }
    }
}