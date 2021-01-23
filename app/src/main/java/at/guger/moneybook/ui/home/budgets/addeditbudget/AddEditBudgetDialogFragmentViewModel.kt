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

package at.guger.moneybook.ui.home.budgets.addeditbudget

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.viewmodel.Event
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.ext.ifNull
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.repository.BudgetsRepository
import kotlinx.coroutines.launch

/**
 * [ViewModel] for the [AddEditBudgetBottomSheetDialogFragment].
 */
class AddEditBudgetDialogFragmentViewModel(private val budgetsRepository: BudgetsRepository) : ViewModel() {

    //region Variables

    private var budget: Budget? = null

    private val _titleRes = MutableLiveData(R.string.NewBudget)
    val titleRes: LiveData<Int> = _titleRes

    val budgetName = MutableLiveData<String>()
    val budgetBudget = MutableLiveData<String>()
    val budgetColor = MutableLiveData(Color.parseColor("#BBDEFB"))

    private val _isValidForm = MutableLiveData<Boolean>()
    val isValidForm: LiveData<Boolean> = _isValidForm

    private val _showColorChooser = MutableLiveData<Event<Int>>()
    val showColorChooser: LiveData<Event<Int>> = _showColorChooser

    private val _budgetSaved = MutableLiveData<Event<Unit>>()
    val budgetSaved: LiveData<Event<Unit>> = _budgetSaved

    //endregion

    //region Methods

    fun setupBudget(budget: Budget) {
        this.budget = budget

        _titleRes.value = R.string.EditBudget

        budgetName.value = budget.name
        budgetBudget.value = CurrencyTextInputEditText.CURRENCY_FORMAT.format(budget.budget)
        budgetColor.value = budget.color
    }

    fun showColorChooser() {
        _showColorChooser.value = Event(budgetColor.value!!)
    }

    fun onTextFieldChanged() {
        _isValidForm.value = !budgetName.value.isNullOrBlank() && parseNumber(budgetBudget.value) > 0.0
    }

    fun save() {
        viewModelScope.launch {
            budget.ifNull {
                budgetsRepository.insert(
                    Budget(
                        name = budgetName.value!!.trim(),
                        budget = parseNumber(budgetBudget.value!!),
                        color = budgetColor.value!!
                    )
                )
            } ?: budgetsRepository.update(
                Budget(
                    id = budget!!.id,
                    name = budgetName.value!!.trim(),
                    budget = parseNumber(budgetBudget.value!!),
                    color = budgetColor.value!!
                )
            )

            _budgetSaved.value = Event(Unit)
        }
    }

    private fun parseNumber(text: String?) = text?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

    //endregion
}