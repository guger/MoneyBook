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

package at.guger.moneybook.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.util.DataUtils
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * [CoroutineWorker] for adding the default account
 */
class DefaultBudgetsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    private val repository: BudgetsRepository by inject()

    override suspend fun doWork(): Result {
        repository.insert(*DataUtils.getDefaultBudgets(applicationContext))

        return Result.success()
    }
}