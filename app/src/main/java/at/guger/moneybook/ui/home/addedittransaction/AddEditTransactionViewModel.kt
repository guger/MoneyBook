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

package at.guger.moneybook.ui.home.addedittransaction

import android.content.Context
import android.provider.Settings
import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.*
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.core.ui.viewmodel.MessageEvent
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.data.repository.AddressBookRepository
import at.guger.moneybook.data.repository.BudgetsRepository
import at.guger.moneybook.data.repository.TransactionsRepository
import at.guger.moneybook.scheduler.reminder.ReminderScheduler
import at.guger.moneybook.util.DateFormatUtils.SHORT_DATE_FORMAT
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * [ViewModel] for the [AddEditTransactionFragment].
 */
class AddEditTransactionViewModel(
    private val transactionsRepository: TransactionsRepository,
    accountsRepository: AccountsRepository,
    budgetsRepository: BudgetsRepository,
    private val addressBookRepository: AddressBookRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    //region Variables

    private var transaction: Transaction? = null

    private val _titleRes = MutableLiveData(R.string.NewTransaction)
    val titleRes: LiveData<Int> = _titleRes

    val transactionTitle = MutableLiveData<String>()
    val transactionAccount = MutableLiveData<String>()
    val transactionBudget = MutableLiveData<String>()
    val transactionType = MutableLiveData(Transaction.TransactionType.EARNING)
    val transactionDate = MutableLiveData(LocalDate.now().format(SHORT_DATE_FORMAT))
    val transactionDueDate = MutableLiveData<String>()
    val transactionValue = MutableLiveData<String>()

    private val _transactionContacts = MutableLiveData<List<String>>()
    val transactionContacts: LiveData<List<String>> = _transactionContacts

    val transactionNotes = MutableLiveData<String>()

    private val _accountsInputVisibility = MutableLiveData(View.VISIBLE)
    val accountsInputVisibility: LiveData<Int> = _accountsInputVisibility

    private val _budgetsInputVisibility = MutableLiveData(View.GONE)
    val budgetsInputVisibility: LiveData<Int> = _budgetsInputVisibility

    private val _contactsInputVisibility = MutableLiveData(View.GONE)
    val contactsInputVisibility: LiveData<Int> = _contactsInputVisibility

    private val _dueDateInputVisibility = MutableLiveData(View.GONE)
    val dueDateInputVisibility: LiveData<Int> = _dueDateInputVisibility

    private val _transferTransactionVisibility = MutableLiveData(false)
    val transferTransactionVisibility: LiveData<Boolean> = _transferTransactionVisibility

    private val _showDatePicker = MutableLiveData<Event<LocalDate>>()
    val showDatePicker: LiveData<Event<LocalDate>> = _showDatePicker

    private val _showDueDatePicker = MutableLiveData<Event<LocalDate>>()
    val showDueDatePicker: LiveData<Event<LocalDate>> = _showDueDatePicker

    private val _showCalculator = MutableLiveData<Event<Unit>>()
    val showCalculator: LiveData<Event<Unit>> = _showCalculator

    private val _showOverlayPermissionDialog = MutableLiveData<Event<List<String>>>()
    val showOverlayPermissionDialog: LiveData<Event<List<String>>> = _showOverlayPermissionDialog

    private val _snackBarMessage = MutableLiveData<MessageEvent>()
    val snackBarMessage: LiveData<MessageEvent> = _snackBarMessage

    private val _snoozeDisabledMessage = MutableLiveData<MessageEvent>()
    val snoozeDisabledMessage: LiveData<MessageEvent> = _snoozeDisabledMessage

    private val _transactionSaved = MutableLiveData<Event<Unit>>()
    val transactionSaved: LiveData<Event<Unit>> = _transactionSaved

    val accounts: LiveData<List<Account>> = accountsRepository.getObservableAccounts()
    val budgets: LiveData<List<Budget>> = budgetsRepository.getObservableBudgets()
    val transferAccounts: LiveData<Pair<List<Account>, Account>> = Transformations.map(accounts) {
        Pair(it, accounts.value?.find { it.name == transactionAccount.value }!!)
    }

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
            transactionDate.value = transaction.date.format(SHORT_DATE_FORMAT)
            transactionValue.value = CurrencyTextInputEditText.CURRENCY_FORMAT.format(value)
            _transactionContacts.value = contacts?.map { it.contactName }
            transactionDueDate.value = transaction.due?.format(SHORT_DATE_FORMAT)
            transactionNotes.value = notes
        }
    }

    fun setupAccount(account: Account) {
        transactionAccount.value = account.name
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

        _dueDateInputVisibility.value = when (transactionType.value) {
            Transaction.TransactionType.CLAIM, Transaction.TransactionType.DEBT -> View.VISIBLE
            else -> View.GONE
        }

        _transferTransactionVisibility.value = when (transactionType.value) {
            Transaction.TransactionType.EXPENSE -> true
            else -> false
        }
    }

    fun showDatePicker() {
        val selectedDate = try {
            LocalDate.parse(transactionDate.value, SHORT_DATE_FORMAT)
        } catch (e: DateTimeParseException) {
            LocalDate.now()
        }

        _showDatePicker.value = Event(selectedDate)
    }

    fun showDueDatePicker() {
        val selectedDate = if (transactionDueDate.value != null) {
            try {
                LocalDate.parse(transactionDueDate.value, SHORT_DATE_FORMAT)
            } catch (e: DateTimeParseException) {
                LocalDate.now().plusDays(5)
            }
        } else {
            LocalDate.now().plusDays(5)
        }

        _showDueDatePicker.value = Event(selectedDate)
    }

    fun showCalculator() {
        _showCalculator.value = Event(Unit)
    }

    fun transferTransaction(context: Context, chippedContacts: List<String>, targetAccount: Account) {
        saveTransaction(context, chippedContacts, broadcastFinished = false)

        saveTransaction(context, chippedContacts, targetAccount = targetAccount)
    }

    @JvmOverloads
    fun saveTransaction(
        context: Context,
        chippedContacts: List<String>,
        ignoreOverlayPermission: Boolean = false,
        broadcastFinished: Boolean = true,
        targetAccount: Account? = null
    ): Pair<Transaction.TransactionEntity, List<Contact>?>? {
        val title = transactionTitle.value?.trim()
        val date = transactionDate.value
        val value = transactionValue.value
        val type = if (targetAccount == null) transactionType.value else Transaction.TransactionType.EARNING
        val dueDate = transactionDueDate.value?.takeIf { it.isNotBlank() }
        val account = targetAccount ?: accounts.value?.find { it.name == transactionAccount.value }
            ?.takeIf { type == Transaction.TransactionType.EARNING || type == Transaction.TransactionType.EXPENSE }
        val budget = budgets.value?.find { it.name == transactionBudget.value }?.takeIf { type == Transaction.TransactionType.EXPENSE }
        val notes = transactionNotes.value?.trim() ?: ""

        if (dueDate != null && Utils.isMarshmallow() && !Settings.canDrawOverlays(context) && !ignoreOverlayPermission) {
            _showOverlayPermissionDialog.value = Event(chippedContacts)

            return null
        }

        val transactionEntity = parseTransactionForm(title, date, value, type, dueDate, account, budget, notes)

        if (transactionEntity != null) {
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
                val id: Long = if (transaction == null) {
                    transactionsRepository.insert(
                        Transaction(
                            entity = transactionEntity,
                            contacts = contacts
                        )
                    )
                } else {
                    transactionsRepository.update(
                        Transaction(
                            entity = transactionEntity.copy(id = transaction!!.id),
                            contacts = contacts
                        )
                    )

                    reminderScheduler.cancelReminder(transaction!!.id)

                    transaction!!.id
                }

                if (type == Transaction.TransactionType.CLAIM || type == Transaction.TransactionType.DEBT) {
                    transactionEntity.due?.let { date -> reminderScheduler.scheduleReminder(id, date) }
                }
            }.invokeOnCompletion {
                if (ignoreOverlayPermission) _snoozeDisabledMessage.value = MessageEvent(R.string.SnoozeDisabled)
                if (broadcastFinished) _transactionSaved.value = Event(Unit)
            }

            return Pair(transactionEntity, contacts)
        }

        return null
    }

    fun checkTransactionForm(): Boolean {
        val title = transactionTitle.value?.trim()
        val date = transactionDate.value
        val value = transactionValue.value
        val type = transactionType.value
        val dueDate = transactionDueDate.value?.takeIf { it.isNotBlank() }
        val account =
            accounts.value?.find { it.name == transactionAccount.value }?.takeIf { type == Transaction.TransactionType.EARNING || type == Transaction.TransactionType.EXPENSE }
        val budget = budgets.value?.find { it.name == transactionBudget.value }?.takeIf { type == Transaction.TransactionType.EXPENSE }
        val notes = transactionNotes.value?.trim() ?: ""

        return parseTransactionForm(title, date, value, type, dueDate, account, budget, notes) != null
    }

    private fun parseTransactionForm(
        title: String?,
        date: String?,
        value: String?,
        type: Int?,
        dueDate: String?,
        account: Account?,
        budget: Budget?,
        notes: String
    ): Transaction.TransactionEntity? {
        if (title.isNullOrBlank()) {
            _snackBarMessage.value = MessageEvent(R.string.EmptyTransactionTitle)

            return null
        }

        val parsedDate: LocalDate? = try {
            LocalDate.parse(date, SHORT_DATE_FORMAT)
        } catch (e: DateTimeParseException) {
            null
        }

        if (parsedDate == null) {
            _snackBarMessage.value = MessageEvent(R.string.InvalidTransactionDate, SHORT_DATE_FORMAT.format(LocalDate.now()))

            return null
        }

        val parsedDueDate: LocalDate? = if (!dueDate.isNullOrBlank()) {
            try {
                LocalDate.parse(dueDate, SHORT_DATE_FORMAT)
            } catch (e: DateTimeParseException) {
                null
            }
        } else {
            null
        }

        if (dueDate?.isNotBlank() == true && parsedDueDate == null) {
            _snackBarMessage.value = MessageEvent(R.string.InvalidTransactionDueDate, SHORT_DATE_FORMAT.format(LocalDate.now()))

            return null
        }

        if ((type == Transaction.TransactionType.CLAIM || type == Transaction.TransactionType.DEBT) && parsedDueDate != null && parsedDueDate.minusDays(1).isBefore(parsedDate)) {
            _snackBarMessage.value = MessageEvent(R.string.TransactionDueDateBeforeDate)

            return null
        }

        val valueNumber = parseNumber(value)

        if (valueNumber <= 0) {
            _snackBarMessage.value = MessageEvent(R.string.InvalidTransactionValue_NotPositive)

            return null
        }

        if (valueNumber > 1E6) {
            _snackBarMessage.value = MessageEvent(R.string.InvalidTransactionValue_TooHigh)

            return null
        }

        return Transaction.TransactionEntity(
            title = title,
            date = parsedDate,
            value = parseNumber(value),
            due = parsedDueDate,
            notes = notes,
            type = type!!,
            accountId = account?.id,
            budgetId = budget?.id
        )
    }

    private fun parseNumber(text: String?) = text?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

    //endregion
}