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

package at.guger.moneybook.scheduler.reminder

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import at.guger.moneybook.R
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.repository.AddressBookRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.util.CurrencyFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * [BroadcastReceiver] receiving actions from a reminder [Notification].
 */
class NotificationReceiver : BroadcastReceiver(), KoinComponent {

    //region Variables

    private val transactionsRepository: TransactionsRepository by inject()
    private val addressBookRepository: AddressBookRepository by inject()

    private val scheduler: ReminderScheduler by inject()

    //endregion

    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = intent.getLongExtra(ReminderScheduler.EXTRA_TRANSACTION_ID, -1)

        when (intent.action) {
            NOTIFICATION_ACTION_SNOOZE -> {
                if (!Utils.isMarshmallow() || Settings.canDrawOverlays(context)) {
                    showSnoozeDialog(context, transactionId)
                } else {
                    Toast.makeText(context, R.string.ScreenOverlayNotPermitted, Toast.LENGTH_LONG).show()
                }
            }
            NOTIFICATION_ACTION_SEND_MESSAGE -> {
                GlobalScope.launch { sendMessage(context, transactionId) }
            }
            NOTIFICATION_ACTION_PAID -> {
                GlobalScope.launch {
                    transactionsRepository.markAsPaid(transactionId)
                    cancelNotification(context, transactionId)
                }
            }
        }
    }

    //region Methods

    private fun showSnoozeDialog(context: Context, transactionId: Long) {
        val dialog = AlertDialog.Builder(context.applicationContext, R.style.OverlayDialogTheme)
            .setTitle(R.string.ChooseDelay)
            .setSingleChoiceItems(R.array.SnoozeDelays, 0, null)
            .setPositiveButton(R.string.Snooze) { dialog, _ ->
                val selectedIndex = (dialog as AlertDialog).listView?.checkedItemPosition ?: 0

                val snoozeDelayText = context.resources.getStringArray(R.array.SnoozeDelays)[selectedIndex]
                val snoozeDelayTime = context.resources.getStringArray(R.array.SnoozeDelayValues)[selectedIndex].toLong()

                GlobalScope.launch { scheduler.scheduleReminder(transactionId, LocalDateTime.now().plus(snoozeDelayTime, ChronoUnit.MILLIS)) }

                cancelNotification(context, transactionId)

                Toast.makeText(context, context.getString(R.string.ReminderDelayed, snoozeDelayText), Toast.LENGTH_LONG).show()
            }
            .setNegativeButton(R.string.Cancel, null)
            .setCancelable(false)
            .create()

        dialog.window?.apply {
            @Suppress("DEPRECATION")
            setType(
                if (Utils.isOreo()) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            )
            setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        dialog.show()
    }

    private suspend fun sendMessage(context: Context, transactionId: Long) {
        val transaction = withContext(Dispatchers.IO) { transactionsRepository.get(transactionId) }

        transaction.run {
            val valueText = CurrencyFormat.format(value)

            val phoneNumbers = addressBookRepository.loadPhoneNumbers(contacts!!.filter { it.paidState == Contact.PaidState.STATE_NOT_PAID }.map { it.contactId }.toSet()).values
            val dataUri = Uri.parse("smsto: ${phoneNumbers.joinToString(";")}")

            val messageIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = dataUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("sms_body", context.getString(R.string.ReminderMessage, valueText))
            }

            messageIntent.resolveActivity(context.packageManager)?.run {
                context.startActivity(messageIntent)

                cancelNotification(context, transactionId)
            } ?: Toast.makeText(context, R.string.NoSMSAppFound, Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelNotification(context: Context, transactionId: Long) {
        NotificationManagerCompat.from(context).cancel(transactionId.toInt())
    }

    //endregion

    companion object {
        const val NOTIFICATION_ACTION_SNOOZE = "at.guger.moneybook.reminder.notification.action.snooze"
        const val NOTIFICATION_ACTION_SEND_MESSAGE = "at.guger.moneybook.reminder.notification.action.sendmessage"
        const val NOTIFICATION_ACTION_PAID = "at.guger.moneybook.reminder.notification.action.paid"
    }
}