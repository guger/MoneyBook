/*
 * Copyright 2019 Daniel Guger
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

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import at.guger.moneybook.BuildConfig
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.core.ui.preference.BasePreferenceFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Main settings fragment.
 */
class MainPreferenceFragment : BasePreferenceFragment() {

    //region Variables

    private lateinit var prefInformation: Preference

    //endregion

    //region PreferenceFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences_main)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefInformation = findPreference(Preferences.INFORMATION)!!

        prefInformation.summary = getString(R.string.prefs_Information, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }

    //endregion

    //region Callback

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            Preferences.PERMISSIONS -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.Permissions)
                    .setMessage(R.string.PermissionDetails)
                    .setPositiveButton(R.string.OK, null)
                    .show()
            }
        }

        return super.onPreferenceTreeClick(preference)
    }

    //endregion
}