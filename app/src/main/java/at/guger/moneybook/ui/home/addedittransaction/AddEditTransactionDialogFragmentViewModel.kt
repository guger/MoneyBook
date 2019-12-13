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

import android.view.View
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
import at.guger.moneybook.data.repository.AddressBookRepository
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.util.DateFormatUtils.MEDIUM_DATE_FORMAT
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

/**
 * [ViewModel] for the [AddEditTransactionDialogFragment].
 */
class AddEditTransactionDialogFragmentViewModel(
    private val transactionsRepository: TransactionsRepository,
    accountsRepository: AccountsRepository,
    budgetsRepository: BudgetsRepository,
    private val addressBookRepository: AddressBookRepository
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

    private val _transactionContacts = MutableLiveData<List<String>>()
    val transactionContacts: LiveData<List<String>> = _transactionContacts

    val transactionNotes = MutableLiveData<String>()

    private val _accountsInputVisibility = MutableLiveData<Int>(View.VISIBLE)
    val accountsInputVisibility: LiveData<Int> = _accountsInputVisibility

    private val _budgetsInputVisibility = MutableLiveData<Int>(View.GONE)
    val budgetsInputVisibility: LiveData<Int> = _budgetsInputVisibility

    private val _contactsInputVisibility = MutableLiveData<Int>(View.GONE)
    val contactsInputVisibility: LiveData<Int> = _contactsInputVisibility

    private val _showDatePicker = MutableLiveData<Event<LocalDate>>()
    val showDatePicker: LiveData<Event<LocalDate>> = _showDatePicker

    private val _showCalculator = MutableLiveData<Event<Unit>>()
    val showCalculator: LiveData<Event<Unit>> = _showCalculator

    private val _snackbarMessage = MutableLiveData<Event<@StringRes Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarMessage

    private val _transactionSaved = MutableLiveData<Event<Unit>>()
    val transactionSaved: LiveData<Event<Unit>> = _transactionSaved

    val accounts: LiveData<List<Account>> = accountsRepository.getAccounts()
    val budgets: LiveData<List<Budget>> = budgetsRepository.getBudgets()

    private val _addressBook = MutableLiveData<Map<Long, String>>()
    val addressBook: LiveData<Map<Long, String>> = _addressBook

    //endregion

    //region Methods

    fun loadContacts() {
        viewModelScope.launch {
            _addressBook.value = addressBookRepository.loadContacts()
        }
    }

    fun setupTransaction(transaction: Transaction) {
        this.transaction = transaction

        _titleRes.value = R.string.EditTransaction

        with(transaction) {
            transactionTitle.value = title
            transactionType.value = type
            transactionAccount.value = account?.name
            transactionBudget.value = budget?.name
            transactionDate.value = transaction.date.format(MEDIUM_DATE_FORMAT)
            transactionValue.value = CurrencyTextInputEditText.CURRENCY_FORMAT.format(value)
            _transactionContacts.value = contacts?.map { it.contactName }
            transactionNotes.value = notes
        }
    }

    fun setupAccount(account: Account) {
        transactionAccount.postValue(account.name)
    }

    fun onTransactionTypeChanged(@IdRes viewId: Int) {
        transactionType.value = when (viewId) {
            R.id.btnAddEditTransactionTypeEarning -> Transaction.TransactionType.EARNING
            R.id.btnAddEditTransactionTypeExpense -> Transaction.TransactionType.EXPENSE
            R.id.btnAddEditTransactionTypeClaim -> Transaction.TransactionType.CLAIM
            else -> Transaction.TransactionType.DEBT
        }

        _accountsInputVisibility.value = when (transactionType.value) {
            Transaction.TransactionType.EARNING, Transaction.TransactionType.EXPENSE -> View.VISIBLE
            else -> View.GONE
        }

        _budgetsInputVisibility.value = when (transactionType.value) {
            Transaction.TransactionType.EXPENSE -> View.VISIBLE
            else -> View.GONE
        }

        _contactsInputVisibility.value = when (transactionType.value) {
            Transaction.TransactionType.CLAIM, Transaction.TransactionType.DEBT -> View.VISIBLE
            else -> View.GONE
        }
    }

    fun showDatePicker() {
        val selectedDate = if (transactionDate.value?.matches(Utils.getShortDatePattern().toRegex()) == true) LocalDate.parse(transactionDate.value, MEDIUM_DATE_FORMAT) else LocalDate.now()

        _showDatePicker.value = Event(selectedDate)
    }

    fun showCalculator() {
        _showCalculator.value = Event(Unit)
    }

    fun saveTransaction(chippedContacts: List<String>) {
        val title = transactionTitle.value!!.trim()
        val date = transactionDate.value
        val value = transactionValue.value
        val type = transactionType.value!!
        val account = accounts.value?.find { it.name == transactionAccount.value }?.takeIf { type == Transaction.TransactionType.EARNING || type == Transaction.TransactionType.EXPENSE }
        val budget = budgets.value?.find { it.name == transactionBudget.value }?.takeIf { type == Transaction.TransactionType.EXPENSE }
        val notes = transactionNotes.value?.trim() ?: ""

        if (validateForm(title, date, value)) {
            val transactionEntity = Transaction.TransactionEntity(
                title = title,
                date = LocalDate.parse(date, MEDIUM_DATE_FORMAT),
                value = parseNumber(value!!),
                notes = notes,
                type = type,
                accountId = account?.id,
                budgetId = budget?.id
            )

            val contacts: List<Contact>? = if (type == Transaction.TransactionType.CLAIM || type == Transaction.TransactionType.DEBT) {
                mutableListOf<Contact>().apply {
                    val addressBookContacts = addressBook.value?.filterValues { chippedContacts.any { contact -> contact == it } }
                    val unsavedContacts = chippedContacts.filterNot { addressBookContacts?.containsValue(it) == true }

                    addressBookContacts?.entries?.forEach {
                        add(
                            Contact(
                                contactId = it.key,
                                contactName = it.value
                            )
                        )
                    }

                    unsavedContacts.forEach {
                        add(Contact(contactName = it))
                    }
                }
            } else {
                null
            }

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

        val valueNumber = parseNumber(value)

        if (valueNumber <= 0) {
            _snackbarMessage.value = Event(R.string.InvalidTransactionValue_NotPositive)

            return false
        }

        if (valueNumber > 1E6) {
            _snackbarMessage.value = Event(R.string.InvalidTransactionValue_TooHigh)

            return false
        }

        return true
    }

    private fun parseNumber(text: String?) = text?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

    //endregion
}