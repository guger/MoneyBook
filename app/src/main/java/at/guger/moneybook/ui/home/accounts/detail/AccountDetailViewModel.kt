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

package at.guger.moneybook.ui.home.accounts.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * [ViewModel] for the [AccountDetailFragment].
 */
class AccountDetailViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val accountsRepository: AccountsRepository,
    private val accountId: Long
) : ViewModel() {

    //region Variables

    private val _account = MutableLiveData<Account>()
    val account: LiveData<Account> = _account

    val transactions: LiveData<List<Transaction>>
    val transactionMonths: LiveData<List<LocalDate>>

    private val _showAddEditTransactionDialogFragment = MutableLiveData<Event<Account>>()
    val showAddEditTransactionDialogFragment: LiveData<Event<Account>> = _showAddEditTransactionDialogFragment

    private val _onItemClick = MutableLiveData<Event<Int>>()
    val onItemClick: LiveData<Event<Int>> = _onItemClick

    private val _onItemLongClick = MutableLiveData<Event<Int>>()
    val onItemLongClick: LiveData<Event<Int>> = _onItemLongClick

    //endregion

    init {
        viewModelScope.launch {
            _account.value = accountsRepository.get(accountId)
        }

        transactions = transactionsRepository.getByAccount(accountId)
        transactionMonths = transactionsRepository.getByAccountMonthly(accountId)
    }

    //region Methods

    fun transactionsByMonth(month: LocalDate): LiveData<List<Transaction>> = transactionsRepository.getByAccountMonth(accountId, month)

    fun showAddEditTransactionDialogFragment() {
        _showAddEditTransactionDialogFragment.value = Event(account.value!!)
    }

    fun delete(vararg transaction: Transaction) {
        viewModelScope.launch {
            transactionsRepository.delete(*transaction)
        }
    }

    fun onItemClick(position: Int) {
        _onItemClick.value = Event(position)
    }

    fun onLongClick(position: Int) {
        _onItemLongClick.value = Event(position)
    }

    //endregion
}