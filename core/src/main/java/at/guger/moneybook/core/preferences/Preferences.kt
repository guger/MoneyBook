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

package at.guger.moneybook.core.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import at.guger.moneybook.core.BuildConfig
import java.util.*

/**
 * Preference Utility class for managing [SharedPreferences].
 */
class Preferences(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var analytics: Boolean
        get() = preferences.getBoolean(ANALYTICS, true)
        set(value) = preferences.edit { putBoolean(ANALYTICS, value) }

    var crashlytics: Boolean
        get() = preferences.getBoolean(CRASHLYTICS, !BuildConfig.DEBUG)
        set(value) = preferences.edit { putBoolean(CRASHLYTICS, value) }

    val currency: Currency = Currency.getInstance(Locale.getDefault())

    companion object {
        const val CURRENCY = "pref_currency"
        const val ANALYTICS = "pref_analytics"
        const val CRASHLYTICS = "pref_crashlytics"

        const val PERMISSIONS = "pref_permissions"
        const val INFORMATION = "pref_information"
    }
}