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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.guger.moneybook.R
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.core.util.ext.colorAttr
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.RemindersRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.ui.main.MainActivity
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * [CoroutineWorker] handling reminder notifications.
 */
class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private val transactionsRepository: TransactionsRepository by inject()
    private val remindersRepository: RemindersRepository by inject()

    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override suspend fun doWork(): Result {
        val transaction = transactionsRepository.get(inputData.getLong(ReminderScheduler.EXTRA_TRANSACTION_ID, -1))

        setupNotificationChannel()

        val notification = makeNotification(transaction)

        remindersRepository.deleteByTransactionId(transaction.id)

        notificationManager.notify(transaction.id.toInt(), notification)

        return Result.success()
    }

    private fun setupNotificationChannel() {
        if (Utils.isOreo()) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    ReminderScheduler.NOTIFICATION_CHANNEL_REMINDER,
                    applicationContext.getString(R.string.notification_channel_reminder),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = applicationContext.getString(R.string.notification_channel_reminder_description)
                }
            )
        }
    }

    private fun makeNotification(transaction: Transaction): Notification {
        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            ReminderScheduler.NOTIFICATION_CHANNEL_REMINDER
        )

        val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        with(notificationBuilder) {
            setContentTitle("MoneyBook Notification")
            setContentText(transaction.title)
            setSmallIcon(R.drawable.ic_notification)
            setAutoCancel(true)
            setContentIntent(PendingIntent.getBroadcast(applicationContext, transaction.id.toInt(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            color = applicationContext.colorAttr(R.attr.colorPrimary)
        }

        return notificationBuilder.build()
    }
}