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

package at.guger.moneybook.ui.home.accounts.addeditaccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.ext.ifNull
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.util.DataUtils
import kotlinx.coroutines.launch

/**
 * [ViewModel] for the [AddEditAccountBottomSheetDialogFragment].
 */
class AddEditAccountDialogFragmentViewModel(private val accountsRepository: AccountsRepository) : ViewModel() {

    //region Variables

    private var account: Account? = null

    private val _titleRes = MutableLiveData(R.string.NewAccount)
    val titleRes: LiveData<Int> = _titleRes

    val accountName = MutableLiveData<String>()
    val accountStartBalance = MutableLiveData<String>()

    private val _accountStartBalanceError = MutableLiveData<Event<Unit>>()
    val accountStartBalanceError: LiveData<Event<Unit>> = _accountStartBalanceError

    private val _accountSaved = MutableLiveData<Event<Unit>>()
    val accountSaved: LiveData<Event<Unit>> = _accountSaved

    //endregion

    //region Methods

    fun setupAccount(account: Account) {
        this.account = account

        _titleRes.value = R.string.EditAccount

        accountName.value = account.name
        accountStartBalance.value = CurrencyTextInputEditText.CURRENCY_FORMAT.format(account.startBalance)
    }

    fun save() {
        viewModelScope.launch {
            val startBalance = parseNumber(accountStartBalance.value)

            if (startBalance != null) {
                account.ifNull {
                    val colors = DataUtils.ACCOUNT_COLORS
                    val accountColor: Int = accountsRepository.countAccounts() % colors.size

                    accountsRepository.insert(
                        Account(
                            name = accountName.value!!.trim(),
                            color = colors[accountColor],
                            startBalance = startBalance
                        )
                    )
                } ?: accountsRepository.update(
                    Account(
                        id = account!!.id,
                        name = accountName.value!!.trim(),
                        color = account!!.color,
                        startBalance = startBalance
                    )
                )

                _accountSaved.value = Event(Unit)
            } else {
                _accountStartBalanceError.value = Event(Unit)
            }
        }
    }

    private fun parseNumber(text: String?) = if (text.isNullOrBlank()) 0.0 else text.replace(",", ".").toDoubleOrNull()

    //endregion
}