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
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.guger.moneybook.R
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.core.util.ext.size
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.RemindersRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.CurrencyFormat
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * [CoroutineWorker] handling reminder notifications.
 */
class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    //region Variables

    private val transactionsRepository: TransactionsRepository by inject()
    private val remindersRepository: RemindersRepository by inject()

    private val notificationManager: NotificationManager by lazy { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    //endregion

    override suspend fun doWork(): Result {
        val transaction = transactionsRepository.get(inputData.getLong(ReminderScheduler.EXTRA_TRANSACTION_ID, -1))

        setupNotificationChannel()

        remindersRepository.deleteByTransactionId(transaction.id)

        notificationManager.notify(transaction.id.toInt(), makeNotification(transaction))

        return Result.success()
    }

    //region Methods

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

        val contentIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(applicationContext).addNextIntent(contentIntent).getPendingIntent(transaction.id.toInt(), PendingIntent.FLAG_UPDATE_CURRENT)

        val title = applicationContext.getString(
            when (transaction.type) {
                Transaction.TransactionType.CLAIM -> R.string.ReminderNotificationTitleClaim
                Transaction.TransactionType.DEBT -> R.string.ReminderNotificationTitleDebt
                else -> throw IllegalStateException("Reminder must not receive transactions of a type other than claim or debt.")
            }
        )

        val contacts = transaction.contacts?.filter { it.paidState == Contact.PaidState.STATE_NOT_PAID }
        val contactsCount = contacts.size()

        val valueText = CurrencyFormat.formatShortened(applicationContext, transaction.value)

        val contentResId = when (transaction.type) {
            Transaction.TransactionType.CLAIM -> {
                when (contactsCount) {
                    0 -> R.string.ReminderNotificationTextClaimNoContact
                    1 -> R.string.ReminderNotificationTextClaimOneContact
                    else -> R.string.ReminderNotificationTextClaimMultipleContacts
                }
            }
            Transaction.TransactionType.DEBT -> {
                when (contactsCount) {
                    0 -> R.string.ReminderNotificationTextDebtNoContact
                    1 -> R.string.ReminderNotificationTextDebtOneContact
                    else -> R.string.ReminderNotificationTextDebtMultipleContacts
                }
            }
            else -> throw IllegalStateException("Reminder must not receive transactions of a type other than claim or debt.")
        }

        val formatArgs = when (contactsCount) {
            0 -> arrayOf(valueText)
            1 -> arrayOf(contacts!!.first().contactName, valueText)
            else -> arrayOf(contactsCount, valueText)
        }

        val contentText = applicationContext.getString(contentResId, *formatArgs)

        with(notificationBuilder) {
            setContentTitle(title)
            setContentText(contentText)
            setSmallIcon(R.drawable.ic_notification)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        }

        val actionIntent = Intent(applicationContext, NotificationReceiver::class.java).apply {
            putExtra(ReminderScheduler.EXTRA_TRANSACTION_ID, transaction.id)
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            transaction.id.toInt(),
            actionIntent.apply { action = NotificationReceiver.NOTIFICATION_ACTION_SNOOZE },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.addAction(NotificationCompat.Action(R.drawable.ic_notification_snooze, applicationContext.getString(R.string.Snooze), snoozePendingIntent))

        if (transaction.type == Transaction.TransactionType.CLAIM && contacts.size() > 0) {
            val messagePendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                transaction.id.toInt(),
                actionIntent.apply { action = NotificationReceiver.NOTIFICATION_ACTION_SEND_MESSAGE },
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            notificationBuilder.addAction(NotificationCompat.Action(R.drawable.ic_notification_message, applicationContext.getString(R.string.Message), messagePendingIntent))
        }

        val paidPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            transaction.id.toInt(),
            actionIntent.apply { action = NotificationReceiver.NOTIFICATION_ACTION_PAID },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.addAction(NotificationCompat.Action(R.drawable.ic_notification_paid, applicationContext.getString(R.string.Paid), paidPendingIntent))

        return notificationBuilder.build()
    }

    //endregion
}