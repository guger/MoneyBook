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

package at.guger.moneybook.ui.home.addedittransaction

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.core.util.ext.ifNull
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.util.Utils.MEDIUM_DATE_FORMAT
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

/**
 * [ViewModel] for the [AddEditTransactionDialogFragment].
 */
class AddEditTransactionDialogFragmentViewModel(
    private val transactionsRepository: TransactionsRepository,
    private val accountsRepository: AccountsRepository,
    private val budgetsRepository: BudgetsRepository
) : ViewModel() {

    //region Variables

    private var transaction: Transaction? = null

    private val _titleRes = MutableLiveData<Int>(R.string.NewTransaction)
    val titleRes: LiveData<Int> = _titleRes

    val transactionTitle = MutableLiveData<String>()
    val transactionAccount = MutableLiveData<String>()
    val transactionBudget = MutableLiveData<String>()
    val transactionType = MutableLiveData<@Transaction.TransactionType Int>(Transaction.TransactionType.EARNING)
    val transactionDate = MutableLiveData<String>(LocalDate.now().format(MEDIUM_DATE_FORMAT))
    val transactionValue = MutableLiveData<String>()
    val transactionContacts = MutableLiveData<String>()
    val transactionNotes = MutableLiveData<String>()

    private val _showDatePicker = MutableLiveData<Event<Unit>>()
    val showDatePicker: LiveData<Event<Unit>> = _showDatePicker

    private val _showCalculator = MutableLiveData<Event<Unit>>()
    val showCalculator: LiveData<Event<Unit>> = _showCalculator

    private val _snackbarMessage = MutableLiveData<Event<@StringRes Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarMessage

    private val _transactionSaved = MutableLiveData<Event<Unit>>()
    val transactionSaved: LiveData<Event<Unit>> = _transactionSaved

    private val _accounts = MutableLiveData<List<Account>>()
    val accounts: LiveData<List<Account>> = _accounts

    private val _budgets = MutableLiveData<List<Budget>>()
    val budgets: LiveData<List<Budget>> = _budgets

    //endregion

    init {
        viewModelScope.launch {
            _accounts.value = accountsRepository.getAccounts()
            _budgets.value = budgetsRepository.getBudgets()
        }
    }

    fun setupTransaction(transaction: Transaction) {
        this.transaction = transaction

        _titleRes.value = R.string.EditTransaction

        with(transaction) {
            transactionTitle.value = title
            transactionType.value = type
            transactionAccount.value = account!!.name
            transactionBudget.value = budget?.name
            transactionDate.value = transaction.date.format(MEDIUM_DATE_FORMAT)
            transactionValue.value = CurrencyTextInputEditText.CURRENCY_FORMAT.format(value)
            transactionNotes.value = notes
        }
    }

    fun onTransactionTypeChanged(@IdRes viewId: Int) {
        transactionType.value = when (viewId) {
            R.id.btnAddEditTransactionTypeEarning -> Transaction.TransactionType.EARNING
            R.id.btnAddEditTransactionTypeExpense -> Transaction.TransactionType.EXPENSE
            R.id.btnAddEditTransactionTypeClaim -> Transaction.TransactionType.CLAIM
            else -> Transaction.TransactionType.DEBT
        }
    }

    fun showDatePicker() {
        _showDatePicker.value = Event(Unit)
    }

    fun showCalculator() {
        _showCalculator.value = Event(Unit)
    }

    fun saveTransaction() {
        val title = transactionTitle.value
        val date = transactionDate.value
        val value = transactionValue.value
        val type = transactionType.value!!
        val account = accounts.value!!.find { it.name == transactionAccount.value }!!
        val budget = budgets.value?.find { it.name == transactionBudget.value }
        val notes = transactionNotes.value ?: ""

        if (validateForm(title, date, value)) {
            val transactionEntity = Transaction.TransactionEntity(
                title = title!!,
                date = LocalDate.parse(date, MEDIUM_DATE_FORMAT),
                value = parseNumber(value!!),
                notes = notes,
                type = type,
                accountId = account.id,
                budgetId = budget?.id
            )

            val contacts: List<Contact> = emptyList()

            viewModelScope.launch {
                transaction.ifNull {
                    transactionsRepository.insert(
                        Transaction(
                            entity = transactionEntity,
                            contacts = contacts
                        )
                    )
                } ?: transactionsRepository.update(
                    Transaction(
                        entity = transactionEntity.copy(id = transaction!!.id),
                        contacts = contacts
                    )
                )
            }.invokeOnCompletion {
                _transactionSaved.value = Event(Unit)
            }
        }
    }

    private fun validateForm(title: String?, date: String?, value: String?): Boolean {
        if (title.isNullOrBlank()) {
            _snackbarMessage.value = Event(R.string.EmptyTransactionTitle)

            return false
        }

        if (date.isNullOrBlank() || !date.matches(Utils.getShortDatePattern().toRegex())) {
            _snackbarMessage.value = Event(R.string.InvalidTransactionDate)

            return false
        }

        if (parseNumber(value) <= 0) {
            _snackbarMessage.value = Event(R.string.InvalidTransactionValue)

            return false
        }

        return true
    }

    private fun parseNumber(text: String?) = text?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
}