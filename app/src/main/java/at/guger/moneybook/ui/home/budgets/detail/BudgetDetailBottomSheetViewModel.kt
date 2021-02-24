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

package at.guger.moneybook.ui.home.budgets.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * [ViewModel] for the [BudgetDetailBottomSheetFragment].
 */
class BudgetDetailBottomSheetViewModel(
    private val transactionsRepository: TransactionsRepository,
    budgetsRepository: BudgetsRepository,
    private val budgetId: Long
) : ViewModel() {

    //region Variables

    val budget: LiveData<Budget> = budgetsRepository.get(budgetId)

    val transactions: LiveData<List<Transaction>> = transactionsRepository.getByBudget(budgetId)

    private val _openDetailFragment = MutableLiveData<Event<Unit>>()
    val openDetailFragment: LiveData<Event<Unit>> = _openDetailFragment

    //endregion

    //region Methods

    fun openDetailFragment() {
        _openDetailFragment.value = Event(Unit)
    }

    fun transactionsByMonth(month: LocalDate): LiveData<List<Transaction>> = transactionsRepository.getByBudgetMonth(budgetId, month)

    fun delete(vararg transaction: Transaction) {
        viewModelScope.launch {
            transactionsRepository.delete(*transaction)
        }
    }

    //endregion
}