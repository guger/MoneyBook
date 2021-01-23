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

package at.guger.moneybook.data.provider.local.dao

import androidx.room.*
import at.guger.moneybook.data.model.Reminder

/**
 * [Dao] method for querying [reminders][Reminder].
 */
@Dao
internal interface ReminderDao {

    @Query("SELECT * FROM reminders")
    suspend fun getReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE transaction_id = :transactionId")
    suspend fun getReminderForTransaction(transactionId: Long): Reminder?

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(vararg reminder: Reminder)

    @Query("DELETE FROM reminders WHERE transaction_id = :transactionId")
    suspend fun deleteByTransactionId(transactionId: Long)
}