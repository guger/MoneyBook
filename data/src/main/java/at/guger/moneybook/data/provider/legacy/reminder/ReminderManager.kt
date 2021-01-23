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

package at.guger.moneybook.data.provider.legacy.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import at.guger.moneybook.data.model.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [BroadcastReceiver] for managing [Reminders][Reminder].
 *
 * @author Daniel Guger
 * @version 1.0
 */
class ReminderManager : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {}

    companion object {

        //region Constants

        const val ACTION_REMINDER = "at.guger.moneybook.action.REMINDER"

        const val EXTRA_BOOKENTRY_ID = "extra_bookentry_id"

        //endregion

        fun cancelReminder(context: Context, bookEntryId: Long) = GlobalScope.launch(Dispatchers.Main) {
            cancel(context, bookEntryId)
            cancelNotification(context, bookEntryId)
        }

        fun cancelNotification(context: Context, bookEntryId: Long) {
            NotificationManagerCompat.from(context).cancel(bookEntryId.toInt())
        }

        private suspend fun cancel(context: Context, bookEntryId: Long) = withContext(Dispatchers.IO) {
            getAlarmManager(context).cancel(makePendingIntent(context, bookEntryId))
        }

        private fun getAlarmManager(context: Context) = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        private fun makePendingIntent(context: Context, bookEntryId: Long): PendingIntent {
            val alarmIntent = Intent(context, ReminderManager::class.java).apply {
                action = ACTION_REMINDER
                putExtra(EXTRA_BOOKENTRY_ID, bookEntryId)
            }
            return PendingIntent.getBroadcast(context, bookEntryId.toInt(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}