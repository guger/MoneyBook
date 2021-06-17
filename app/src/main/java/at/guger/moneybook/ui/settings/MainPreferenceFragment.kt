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

package at.guger.moneybook.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import at.guger.moneybook.BuildConfig
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.core.ui.preference.BasePreferenceFragment
import at.guger.moneybook.data.crypto.Crypto
import at.guger.moneybook.work.ExportImportWorker
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

/**
 * Main settings fragment.
 */
class MainPreferenceFragment : BasePreferenceFragment() {

    //region Variables

    private var requireRestart: Boolean = false

    private lateinit var prefCurrency: ListPreference
    private lateinit var prefInformation: Preference

    private var restartSnackbar: Snackbar? = null

    private var password: String? = null
    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            exportData(uri, password!!)
        } else {
            Snackbar.make(requireView(), R.string.NoBackupFileLocationSelected, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.mBottomAppBar)
                .show()
        }
    }

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importData(uri, password!!)
        } else {
            Snackbar.make(requireView(), R.string.NoBackupFileLocationSelected, Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.mBottomAppBar)
                .show()
        }
    }

    //endregion

    //region PreferenceFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences_main)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefCurrency = findPreference(Preferences.CURRENCY)!!
        prefInformation = findPreference(Preferences.EXPORT_IMPORT)!!
        prefInformation = findPreference(Preferences.INFORMATION)!!

        prefCurrency.setOnPreferenceChangeListener { _, _ ->
            requireRestart = true
            showRestartSnackBar()

            true
        }

        prefInformation.summary = getString(
            R.string.prefs_Information,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        if (requireRestart) restartApplication()
    }

    //endregion

    //region Methods

    private fun exportOrImportData() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ExportImport)
            .setMessage(R.string.ExportImportMessage)
            .setNegativeButton(R.string.Export) { _, _ ->
                requestPassword(ExportImportWorker.EXPORT)
            }
            .setPositiveButton(R.string.Import) { _, _ ->
                requestPassword(ExportImportWorker.IMPORT)
            }
            .show()
    }

    private fun requestPassword(operation: String) {
        MaterialDialog(requireContext()).show {
            title(res = R.string.EnterPassword)
            message(res = R.string.EnterPasswordMessage)
            input(
                waitForPositiveButton = false,
                hintRes = R.string.Password,
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD,
                maxLength = Crypto.BYTEARRAY_LENGTH
            ) { dialog, text ->
                val isValid = text.trim().length == Crypto.BYTEARRAY_LENGTH

                dialog.setActionButtonEnabled(WhichButton.POSITIVE, isValid)
            }
            positiveButton(res = R.string.OK) {
                password = it.getInputField().text.toString()

                when (operation) {
                    ExportImportWorker.EXPORT -> {
                        val today = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

                        createDocument.launch("${formatter.format(today)}.${ExportImportWorker.BACKUP_FILE_EXTENSION}")
                    }
                    else -> {
                        openDocument.launch(arrayOf("*/*"))
                    }
                }
            }
            negativeButton(res = R.string.Cancel) {
                Snackbar.make(requireView(), if (operation == ExportImportWorker.EXPORT) R.string.ExportAborted else R.string.ImportAborted, Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.mBottomAppBar)
                    .show()
            }
        }
    }

    private fun exportData(uri: Uri, password: String) {
        WorkManager.getInstance(requireContext())
            .enqueue(
                OneTimeWorkRequestBuilder<ExportImportWorker>()
                    .setInputData(
                        workDataOf(
                            ExportImportWorker.OPERATION to ExportImportWorker.EXPORT,
                            ExportImportWorker.FILE_URI to uri.toString(),
                            ExportImportWorker.PASSWORD to password
                        )
                    )
                    .build()
            )
    }

    private fun importData(uri: Uri, password: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.ImportData)
            .setMessage(R.string.ImportDataOverwriteAll)
            .setPositiveButton(R.string.Import) { _, _ ->
                WorkManager.getInstance(requireContext())
                    .enqueue(
                        OneTimeWorkRequestBuilder<ExportImportWorker>()
                            .setInputData(
                                workDataOf(
                                    ExportImportWorker.OPERATION to ExportImportWorker.IMPORT,
                                    ExportImportWorker.FILE_URI to uri.toString(),
                                    ExportImportWorker.PASSWORD to password
                                )
                            )
                            .build()
                    )
            }
            .setNegativeButton(R.string.Cancel, null)
            .show()
    }

    private fun showRestartSnackBar() {
        restartSnackbar = Snackbar.make(
            requireView(),
            R.string.PreferencesRestartRequired,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAnchorView(R.id.mBottomAppBar)
            .setAction(R.string.Restart) {
                restartApplication()
            }
            .also { it.show() }
    }

    private fun restartApplication() {
        val restartIntent: Intent? = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)?.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }

        startActivity(restartIntent)
        exitProcess(0)
    }

    //endregion

    //region Callback

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            Preferences.ANALYTICS -> {
                Firebase.analytics.setAnalyticsCollectionEnabled((preference as SwitchPreference).isChecked)
            }
            Preferences.CRASHLYTICS -> {
                Firebase.crashlytics.setCrashlyticsCollectionEnabled((preference as SwitchPreference).isChecked)
            }
            Preferences.PERMISSIONS -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.Permissions)
                    .setMessage(R.string.PermissionDetails)
                    .setPositiveButton(R.string.Close, null)
                    .show()
            }
            Preferences.EXPORT_IMPORT -> {
                exportOrImportData()
            }
            Preferences.INFORMATION -> startActivity(
                Intent.parseUri(
                    "https://github.com/guger/MoneyBook",
                    0
                )
            )
        }

        return super.onPreferenceTreeClick(preference)
    }

    //endregion
}