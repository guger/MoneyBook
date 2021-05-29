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
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import at.guger.moneybook.BuildConfig
import at.guger.moneybook.R
import at.guger.moneybook.core.permission.Permission
import at.guger.moneybook.core.permission.PermissionManager
import at.guger.moneybook.core.permission.RationaleInfo
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.core.ui.preference.BasePreferenceFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlin.system.exitProcess


/**
 * Main settings fragment.
 */
class MainPreferenceFragment : BasePreferenceFragment() {

    //region Variables

    private var requireRestart: Boolean = false

    private lateinit var prefCurrency: ListPreference
    private lateinit var prefExportImport: Preference
    private lateinit var prefInformation: Preference

    private val permissionManager = PermissionManager.from(this)

    private var restartSnackbar: Snackbar? = null

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
        permissionManager.requestPermission(
            Permission.STORAGE,
            info = RationaleInfo(R.string.StoragePermission, R.string.StoragePermissionNeeded)
        ) { isAllGranted, _ ->
            if (isAllGranted) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.ExportImport)
                    .setItems(R.array.ExportImport) { _, which ->
                        when (which) {
                            0 -> exportData()
                            1 -> importData()
                        }
                    }
                    .show()
            } else {
                Snackbar.make(requireView(), R.string.StoragePermissionDeniedExportImportNotPossible, Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.mBottomAppBar)
                    .show()
            }
        }
    }

    private fun exportData() {
        Toast.makeText(requireContext(), "Export Data", Toast.LENGTH_SHORT).show()
    }

    private fun importData() {
        Toast.makeText(requireContext(), "Import Data", Toast.LENGTH_SHORT).show()
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