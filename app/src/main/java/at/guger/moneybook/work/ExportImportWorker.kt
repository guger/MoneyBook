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
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import at.guger.moneybook.R
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.data.crypto.Crypto
import at.guger.moneybook.data.json.ExportImportConverter
import at.guger.moneybook.data.repository.ExportImportRepository
import at.guger.moneybook.scheduler.reminder.ReminderScheduler
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.crypto.SecretKey

/**
 * [CoroutineWorker] for exporting or importing data.
 */
class ExportImportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    //region Variables

    private val exportImportRepository: ExportImportRepository by inject()

    private val exportImportConverter = ExportImportConverter()

    private val fileUri = Uri.parse(inputData.getString(FILE_URI))

    private val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val reminderScheduler: ReminderScheduler by inject()

    private val operation = inputData.getString(OPERATION)!!

    //endregion

    //region Methods

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(operation))

        val key = Crypto.getKeyFromPassword(inputData.getString(PASSWORD)!!)

        return when (operation) {
            EXPORT -> export(key)
            IMPORT -> import(key)
            else -> Result.failure()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun export(key: SecretKey): Result {
        val transactionEntities = exportImportRepository.getTransactionEntities()
        val accounts = exportImportRepository.getAccounts()
        val contacts = exportImportRepository.getContacts()
        val reminders = exportImportRepository.getReminders()
        val budgets = exportImportRepository.getBudgets()

        val export = exportImportConverter.convertExport(ExportImportConverter.ExportImportModel(transactionEntities, accounts, contacts, reminders, budgets))
        val encryptedExport = Crypto.encrypt(key, export.toByteArray())

        try {
            applicationContext.contentResolver.openFileDescriptor(fileUri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { stream ->
                    stream.write(encryptedExport)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return Result.failure()
        } catch (e: IOException) {
            e.printStackTrace()
            return Result.failure()
        }

        return Result.success()
    }

    private suspend fun import(key: SecretKey): Result {
        val importModel = try {
            val decryptedImport = Crypto.decrypt(key, readFile(fileUri))
            exportImportConverter.convertImport(String(decryptedImport))
        } catch (e: javax.crypto.IllegalBlockSizeException) {
            Firebase.crashlytics.run {
                recordException(e)
            }

            notifyImportFailed()

            return Result.failure()
        } catch (e: Exception) {
            Firebase.crashlytics.run {
                log("Invalid file content: $fileUri")
                recordException(e)
            }

            notifyImportFailed()

            return Result.failure()
        }

        exportImportRepository.deleteAll()

        exportImportRepository.insertAccounts(importModel.accounts)
        exportImportRepository.insertBudgets(importModel.budgets)
        exportImportRepository.insertTransactions(importModel.transactionEntities)
        exportImportRepository.insertContacts(importModel.contacts)

        importModel.reminders.forEach {
            reminderScheduler.scheduleReminder(it.transactionId, it.date)
        }

        return Result.success()
    }

    private fun readFile(uri: Uri): ByteArray {
        return applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: throw IllegalArgumentException("$uri does not point to a valid file.")
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
        }

        return notificationBuilder.build()
    }

    private fun notifyImportFailed() {
        val notificationBuilder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_EXPORT_IMPORT)

        with(notificationBuilder) {
            setContentTitle(applicationContext.getString(R.string.ImportFailed))
            setContentText(applicationContext.getString(R.string.ImportFailedMessage, BACKUP_FILE_EXTENSION))
            setStyle(NotificationCompat.BigTextStyle().bigText(applicationContext.getString(R.string.ImportFailedMessage, BACKUP_FILE_EXTENSION)))
            setSmallIcon(R.drawable.ic_export_import)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        notificationManager.notify(EXPORT_IMPORT_FAILED_NOTIFICATION_ID, notificationBuilder.build())
    }

    //endregion

    companion object {
        const val OPERATION = "export_import_operation"
        const val FILE_URI = "export_import_file_uri"
        const val PASSWORD = "export_import_password"
        const val EXPORT = "operation_export"
        const val IMPORT = "operation_import"

        const val BACKUP_FILE_EXTENSION = "MoneyBookBackup"

        const val NOTIFICATION_CHANNEL_EXPORT_IMPORT = "at.guger.moneybook.notification.EXPORT_IMPORT"

        private const val EXPORT_IMPORT_NOTIFICATION_ID = 2001
        private const val EXPORT_IMPORT_FAILED_NOTIFICATION_ID = 2002
    }
}