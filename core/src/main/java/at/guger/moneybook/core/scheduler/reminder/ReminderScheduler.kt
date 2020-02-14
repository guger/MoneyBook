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

package at.guger.moneybook.core.scheduler.reminder

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import at.guger.moneybook.core.util.ext.toEpochMilli
import at.guger.moneybook.data.model.Reminder
import at.guger.moneybook.data.repository.RemindersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Scheduler class for managing [reminders][Reminder].
 */
class ReminderScheduler(private val context: Context, private val repository: RemindersRepository) {

    //region Methods

    suspend fun scheduleReminder(transactionId: Long, date: LocalDate) {
        val work = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(workDataOf(EXTRA_TRANSACTION_ID to transactionId))
            .setInitialDelay(calculateDelay(date), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(makeWorkerName(transactionId), ExistingWorkPolicy.REPLACE, work)

        withContext(Dispatchers.IO) {
            repository.getReminderForTransaction(transactionId)?.let {
                repository.update(it.copy(date = date))
            } ?: repository.insert(Reminder(transactionId = transactionId, date = date))
        }
    }

    suspend fun cancelReminder(transactionId: Long) = withContext(Dispatchers.IO) {
        repository.getReminderForTransaction(transactionId)?.let {
            WorkManager.getInstance(context).cancelUniqueWork(makeWorkerName(transactionId))

            repository.delete(it)
        } ?: throw IllegalStateException("Attempt to cancel a not-existing reminder.")
    }

    private fun calculateDelay(date: LocalDate): Long {
        return date.atTime(12, 0).toEpochMilli() - LocalDateTime.now().toEpochMilli()
    }

    private fun makeIntent(transactionId: Long): Intent {
        return Intent(context, ReminderScheduler::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_TRANSACTION_ID, transactionId)
        }
    }

    private fun makePendingIntent(transactionId: Long): PendingIntent {
        return PendingIntent.getBroadcast(context, transactionId.toInt(), makeIntent(transactionId), PendingIntent.FLAG_UPDATE_CURRENT)
    }

    //endregion

    companion object {
        private const val REMINDER_WORKER_NAME_TEMPLATE = "at.guger.moneybook.reminder.%d"

        fun makeWorkerName(transactionId: Long) = REMINDER_WORKER_NAME_TEMPLATE.format(transactionId)

        const val ACTION_REMINDER = "at.guger.moneybook.action.REMINDER"

        const val NOTIFICATION_CHANNEL_REMINDER = "at.guger.moneybook.notification.REMINDER"

        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"
    }
}