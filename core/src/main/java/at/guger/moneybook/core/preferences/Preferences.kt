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

package at.guger.moneybook.core.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import at.guger.moneybook.core.BuildConfig
import at.guger.moneybook.core.util.Utils
import java.util.*

/**
 * Preference utility class for managing [SharedPreferences].
 */
class Preferences(private val context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var firstStart: Boolean
        get() = preferences.getBoolean(FIRST_START, true)
        set(value) = preferences.edit { putBoolean(FIRST_START, value) }

    var analytics: Boolean
        get() = preferences.getBoolean(ANALYTICS, true)
        set(value) = preferences.edit { putBoolean(ANALYTICS, value) }

    var biometricAuth: Boolean
        get() = preferences.getBoolean(BIOMETRIC_AUTH, false).takeIf { Utils.hasBiometricFeatures(context) } ?: false
        set(value) = preferences.edit { putBoolean(BIOMETRIC_AUTH, value) }

    var crashlytics: Boolean
        get() = preferences.getBoolean(CRASHLYTICS, !BuildConfig.DEBUG)
        set(value) = preferences.edit { putBoolean(CRASHLYTICS, value) }

    var experimental: Boolean
        get() = preferences.getBoolean(EXPERIMENTAL, BuildConfig.DEBUG)
        set(value) = preferences.edit { putBoolean(EXPERIMENTAL, value) }

    var currency: Currency?
        get() {
            return try {
                preferences.getString(CURRENCY, null)?.takeIf { it != "Default" }?.let(Currency::getInstance) ?: Currency.getInstance(Locale.getDefault())
            } catch (e: IllegalArgumentException) {
                null // This is for rare cases, where the default currency is not available.
            }
        }
        set(value) = preferences.edit { putString(CURRENCY, value!!.currencyCode) }

    companion object {
        const val FIRST_START = "pref_firstStart"
        const val CURRENCY = "pref_currency"
        const val ANALYTICS = "pref_analytics"
        const val AUTHENTICATION = "pref_cat_authentication"
        const val BIOMETRIC_AUTH = "pref_biometric_auth"
        const val CRASHLYTICS = "pref_crashlytics"
        const val EXPERIMENTAL = "pref_experimental"

        const val PERMISSIONS = "pref_permissions"
        const val EXPORT_IMPORT = "pref_export_import"
        const val INFORMATION = "pref_information"
    }
}