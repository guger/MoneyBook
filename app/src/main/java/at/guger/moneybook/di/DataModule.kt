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

package at.guger.moneybook.di

import androidx.work.OneTimeWorkRequestBuilder
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.ContactsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.work.DefaultAccountWorker
import at.guger.moneybook.work.DefaultBudgetsWorker
import org.koin.dsl.module

/**
 * Dependency injection module for data concerning elements.
 */

val dataModule = module {
    single {
        AppDatabase.get(
            get(),
            listOf(
                OneTimeWorkRequestBuilder<DefaultAccountWorker>().build(),
                OneTimeWorkRequestBuilder<DefaultBudgetsWorker>().build()
            )
        )
    }

    single { TransactionsRepository(get()) }
    single { AccountsRepository(get()) }
    single { BudgetsRepository(get()) }
    single { ContactsRepository(get()) }
}