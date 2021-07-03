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

package at.guger.moneybook.di

import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.scheduler.reminder.ForceStopWorker
import at.guger.moneybook.scheduler.reminder.ReminderScheduler
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.home.accounts.addeditaccount.AddEditAccountDialogFragmentViewModel
import at.guger.moneybook.ui.home.accounts.detail.AccountDetailViewModel
import at.guger.moneybook.ui.home.addedittransaction.AddEditTransactionViewModel
import at.guger.moneybook.ui.home.budgets.addeditbudget.AddEditBudgetDialogFragmentViewModel
import at.guger.moneybook.ui.home.budgets.detail.BudgetDetailBottomSheetViewModel
import at.guger.moneybook.ui.home.budgets.detail.BudgetDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

/**
 * Dependency injection module for presentation elements.
 */

val appModule = module {

    single { Preferences(get()) }
    single { ReminderScheduler(get(), get()) }

    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { (accountId: Long) -> AccountDetailViewModel(get(), get(), accountId) }
    viewModel { (budgetId: Long) -> BudgetDetailViewModel(get(), get(), budgetId) }
    viewModel { (budgetId: Long) -> BudgetDetailBottomSheetViewModel(get(), get(), budgetId) }
    viewModel { AddEditTransactionViewModel(get(), get(), get(), get(), get()) }
    viewModel { AddEditAccountDialogFragmentViewModel(get()) }
    viewModel { AddEditBudgetDialogFragmentViewModel(get()) }
}