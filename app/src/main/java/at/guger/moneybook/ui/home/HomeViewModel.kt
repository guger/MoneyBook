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

package at.guger.moneybook.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.scheduler.reminder.ReminderScheduler
import kotlinx.coroutines.launch

/**
 * [ViewModel] for the home fragment and it's sub fragments.
 */
class HomeViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val accountsRepository: AccountsRepository,
    private val budgetsRepository: BudgetsRepository,
    private val scheduler: ReminderScheduler
) : ViewModel() {

    //region Variables

    val claimsAndDebts = transactionsRepository.getClaimsAndDebts()

    val accounts = accountsRepository.getObservableAccountsWithBalance()

    val budgetsWithBalance = budgetsRepository.getBudgetsWithBalance()

    private val _navigateToPage = MutableLiveData<Event<HomeFragment.Destination>>()
    val navigateToPage: LiveData<Event<HomeFragment.Destination>> = _navigateToPage

    private val _moveToAccount = MutableLiveData<Event<Pair<Array<out Transaction>, List<AccountWithBalance>>>>()
    val moveToAccount: LiveData<Event<Pair<Array<out Transaction>, List<AccountWithBalance>>>> = _moveToAccount

    private val _showAccount = MutableLiveData<Event<Long>>()
    val showAccount: LiveData<Event<Long>> = _showAccount

    //endregion

    //region Methods

    fun navigateTo(destination: HomeFragment.Destination) {
        _navigateToPage.value = Event(destination)
    }

    fun showAccount(account: AccountWithBalance) {
        _showAccount.value = Event(account.id)
    }

    fun markAsPaid(vararg transaction: Transaction, moveToAccount: Boolean = false) {
        viewModelScope.launch {
            if (moveToAccount) {
                _moveToAccount.value = Event(Pair(transaction, accounts.value!!))
            } else {
                transaction.forEach {
                    transactionsRepository.markAsPaid(it)

                    if (it.due != null) scheduler.cancelReminder(it.id)
                }
            }
        }
    }

    fun moveToAccount(account: Account, vararg transaction: Transaction) {
        viewModelScope.launch {
            transaction.forEach {
                transactionsRepository.moveToAccount(it, account.id)

                if (it.due != null) scheduler.cancelReminder(it.id)
            }
        }
    }

    fun deleteTransaction(vararg transaction: Transaction) {
        viewModelScope.launch { transactionsRepository.delete(*transaction) }
    }

    fun deleteAccount(vararg account: Account) {
        viewModelScope.launch { accountsRepository.delete(*account) }
    }

    fun deleteBudget(vararg budget: Budget) {
        viewModelScope.launch { budgetsRepository.delete(*budget) }
    }

    //endregion
}