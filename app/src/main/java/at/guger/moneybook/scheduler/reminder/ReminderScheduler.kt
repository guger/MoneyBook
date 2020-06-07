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

package at.guger.moneybook.scheduler.reminder

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import at.guger.moneybook.core.util.ext.toEpochMilli
import at.guger.moneybook.data.model.Reminder
import at.guger.moneybook.data.repository.RemindersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Scheduler class for managing [reminders][Reminder].
 */
class ReminderScheduler(private val context: Context, private val repository: RemindersRepository) {

    private val alarmManager: AlarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    //region Methods

    suspend fun scheduleReminder(transactionId: Long, date: LocalDate) {
        scheduleReminder(transactionId, calculateReminderTime(date))
    }

    suspend fun scheduleReminder(transactionId: Long, date: LocalDateTime) {
        AlarmManagerCompat.setAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            date.toEpochMilli(),
            makePendingIntent(context, transactionId)
        )

        withContext(Dispatchers.IO) {
            repository.getReminderForTransaction(transactionId)?.let {
                repository.update(it.copy(date = date))
            } ?: repository.insert(Reminder(transactionId = transactionId, date = date))
        }
    }

    suspend fun cancelReminder(transactionId: Long) = withContext(Dispatchers.IO) {
        repository.getReminderForTransaction(transactionId)?.let {
            alarmManager.cancel(makePendingIntent(context, transactionId))
            notificationManager.cancel(transactionId.toInt())

            repository.delete(it)
        }
    }

    private fun calculateReminderTime(date: LocalDate): LocalDateTime = date.atTime(12, 0)

    //endregion

    companion object {
        const val ACTION_REMINDER = "at.guger.moneybook.action.REMINDER"

        const val NOTIFICATION_CHANNEL_REMINDER = "at.guger.moneybook.notification.REMINDER"

        const val EXTRA_TRANSACTION_ID = "extra_transaction_id"

        private fun makePendingIntent(context: Context, transactionId: Long): PendingIntent {
            val reminderIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
                putExtra(EXTRA_TRANSACTION_ID, transactionId)
            }

            return PendingIntent.getBroadcast(context, transactionId.toInt(), reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
