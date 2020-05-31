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

import android.content.BroadcastReceiver
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * [BroadcastReceiver] receiving actions from a reminder [Notification].
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = check(intent.getLongExtra(ReminderScheduler.EXTRA_TRANSACTION_ID, -1) != -1L)

        when (intent.action) {
            NOTIFICATION_ACTION_SNOOZE -> {
                Toast.makeText(context, "Snooze Id: $transactionId", Toast.LENGTH_LONG).show()
            }
            NOTIFICATION_ACTION_SEND_MESSAGE -> {
                Toast.makeText(context, "Message Id: $transactionId", Toast.LENGTH_LONG).show()
            }
            NOTIFICATION_ACTION_PAID -> {
                Toast.makeText(context, "Paid Id: $transactionId", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val NOTIFICATION_ACTION_SNOOZE = "at.guger.moneybook.reminder.notification.action.snooze"
        const val NOTIFICATION_ACTION_SEND_MESSAGE = "at.guger.moneybook.reminder.notification.action.sendmessage"
        const val NOTIFICATION_ACTION_PAID = "at.guger.moneybook.reminder.notification.action.paid"
    }
}