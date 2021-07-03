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

package at.guger.moneybook

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.di.appModule
import at.guger.moneybook.di.dataModule
import at.guger.moneybook.di.mainModule
import at.guger.moneybook.scheduler.reminder.ForceStopWorker
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinExperimentalAPI
import org.koin.core.context.startKoin

/**
 * Main application class.
 */
class MoneyBook : Application() {

    private val preferences: Preferences by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MoneyBook.applicationContext)
            modules(listOf(mainModule, dataModule, appModule))
        }

        if (preferences.analytics) FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)

        if (preferences.crashlytics) FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        checkForceStopped()
    }

    private fun checkForceStopped() {
        if (ForceStopWorker.isForceStopped(applicationContext)) WorkManager.getInstance(applicationContext).enqueue(OneTimeWorkRequestBuilder<ForceStopWorker>().build())
    }
}