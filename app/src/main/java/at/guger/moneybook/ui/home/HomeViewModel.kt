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

package at.guger.moneybook.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.util.DataUtils

/**
 * [ViewModel] for the home fragment and it's sub fragments.
 */
class HomeViewModel(private val transactionsRepository: TransactionsRepository, private val accountsRepository: AccountsRepository) : ViewModel() {

    //region Variables

    val transactions: LiveData<List<Transaction>>

    val coloredAccounts: LiveData<List<ColoredAccount>>

    private val _navigateToPage = MutableLiveData<Event<HomeFragment.Destination>>()
    val navigateToPage: LiveData<Event<HomeFragment.Destination>> = _navigateToPage

    private val _showAccount = MutableLiveData<Event<Long>>()
    val showAccount: LiveData<Event<Long>> = _showAccount

    //endregion

    init {
        val colors = DataUtils.getAccountColors()

        transactions = transactionsRepository.getObservableTransactions()
        coloredAccounts = Transformations.map(accountsRepository.getObservableAccountsWithBalance()) { accounts ->
            accounts.mapIndexed { index, accountWithBalance -> ColoredAccount(accountWithBalance, colors[index]) }
        }
    }

    //region Methods

    fun navigateTo(destination: HomeFragment.Destination) {
        _navigateToPage.value = Event(destination)
    }

    fun showAccount(account: AccountWithBalance) {
        _showAccount.value = Event(account.id)
    }

    //endregion
}