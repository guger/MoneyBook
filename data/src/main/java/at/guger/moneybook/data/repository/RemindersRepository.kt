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

import at.guger.moneybook.data.model.Reminder
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.ReminderDao

/**
 * Repository class for handling [reminders][Reminder].
 */
class RemindersRepository(database: AppDatabase) {

    //region Variables

    private val reminderDao: ReminderDao = database.reminderDao()

    //endregion

    //region Methods

    suspend fun getReminders(): List<Reminder> {
        return reminderDao.getReminders()
    }

    suspend fun getReminderForTransaction(transactionId: Long): Reminder? {
        return reminderDao.getReminderForTransaction(transactionId)
    }

    suspend fun insert(vararg reminder: Reminder) {
        reminderDao.insert(*reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.update(reminder)
    }

    suspend fun delete(vararg reminder: Reminder) {
        reminderDao.delete(*reminder)
    }

    suspend fun deleteByTransactionId(transactionId: Long) {
        reminderDao.deleteByTransactionId(transactionId)
    }

    //endregion
}