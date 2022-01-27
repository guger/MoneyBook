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

package at.guger.moneybook.scheduler.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.app.AlarmManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.guger.moneybook.core.util.ext.makeImmutableFlag
import at.guger.moneybook.core.util.ext.makeMutableFlag
import at.guger.moneybook.core.util.ext.toEpochMilli
import at.guger.moneybook.data.repository.RemindersRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime

class ForceStopWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    //region Variables

    private val remindersRepository: RemindersRepository by inject()
    private val scheduler: ReminderScheduler by inject()

    //endregion

    override suspend fun doWork(): Result {
        remindersRepository.getReminders().forEach { reminder ->
            scheduler.scheduleReminder(
                reminder.transactionId,
                reminder.date
            )
        }

        return Result.success()
    }

    class ForceStopReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context != null && intent?.action == ACTION_FORCE_STOP_RESCHEDULE && isForceStopped(context)) {
                setForceStopAlarm(context)
            }
        }
    }

    companion object {
        private const val ACTION_FORCE_STOP_RESCHEDULE_ID = 10100
        private const val ACTION_FORCE_STOP_RESCHEDULE =
            "at.guger.moneybook.action.FORCE_STOP_RESCHEDULE"

        private const val FORCE_STOP_DELAY_YEARS = 10L

        private fun makeForceStopPendingIntent(context: Context, flags: Int): PendingIntent? {
            val forceStopIntent = Intent(context, ForceStopReceiver::class.java).apply {
                action = ACTION_FORCE_STOP_RESCHEDULE
            }

            return PendingIntent.getBroadcast(
                context,
                ACTION_FORCE_STOP_RESCHEDULE_ID,
                forceStopIntent,
                flags.makeMutableFlag()
            )
        }

        fun setForceStopAlarm(context: Context) {
            val fireDate = LocalDateTime.now().plusYears(FORCE_STOP_DELAY_YEARS).toEpochMilli()

            context.registerReceiver(
                ForceStopReceiver(),
                IntentFilter(ACTION_FORCE_STOP_RESCHEDULE)
            )

            AlarmManagerCompat.setAndAllowWhileIdle(
                getAlarmManager(context),
                AlarmManager.RTC_WAKEUP,
                fireDate,
                makeForceStopPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)!!
            )
        }

        private fun getAlarmManager(context: Context) =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun isForceStopped(context: Context): Boolean {
            return if (makeForceStopPendingIntent(context, PendingIntent.FLAG_NO_CREATE) == null) {
                setForceStopAlarm(context)
                true
            } else {
                false
            }
        }


    }
}