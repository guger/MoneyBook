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

package at.guger.moneybook.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.net.toFile
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import at.guger.moneybook.R
import at.guger.moneybook.core.util.Utils
import kotlinx.coroutines.delay

/**
 * [CoroutineWorker] for exporting or importing data.
 */
class ExportImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val fileUri = Uri.parse(inputData.getString(FILE_URI))

    private val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val operation = inputData.getString(OPERATION)!!

        setForeground(createForegroundInfo(operation))

        return when (operation) {
            EXPORT -> export()
            IMPORT -> import()
            else -> Result.failure()
        }
    }

    private suspend fun export(): Result {
        // TODO
        delay(5000)
        return Result.success()
    }

    private suspend fun import(): Result {
        //TODO
        delay(7000)
        return Result.success()
    }

    private fun createForegroundInfo(operation: String): ForegroundInfo {
        return ForegroundInfo(EXPORT_IMPORT_NOTIFICATION_ID, makeNotification(operation))
    }

    private fun makeNotification(operation: String): Notification {
        if (Utils.isOreo()) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_EXPORT_IMPORT,
                    applicationContext.getString(R.string.notification_channel_export_import),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = applicationContext.getString(R.string.notification_channel_export_import_description)
                }
            )
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_EXPORT_IMPORT)

        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val title = applicationContext.getString(
            when (operation) {
                EXPORT -> R.string.OngoingExport
                IMPORT -> R.string.OngoingImport
                else -> throw IllegalStateException("Operation \"$operation\" is not valid.")
            }
        )

        with(notificationBuilder) {
            setContentTitle(title)
            setSmallIcon(R.drawable.ic_export_import)
            setProgress(0, 0, true)
            addAction(R.drawable.ic_close, applicationContext.getString(R.string.Cancel), cancelIntent)
        }

        return notificationBuilder.build()
    }

    companion object {
        const val OPERATION = "export_import_operation"
        const val FILE_URI = "export_import_file_uri"
        const val EXPORT = "operation_export"
        const val IMPORT = "operation_import"

        const val BACKUP_FILE_EXTENSION = "MoneyBookBackup"

        private const val NOTIFICATION_CHANNEL_EXPORT_IMPORT = "at.guger.moneybook.notification.EXPORT_IMPORT"

        private const val EXPORT_IMPORT_NOTIFICATION_ID = 2001

        fun checkValidBackupFile(backupFileUri: Uri): Boolean {
            return backupFileUri.toString().endsWith(BACKUP_FILE_EXTENSION)
        }
    }
}