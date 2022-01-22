/*
 * Copyright 2022 Daniel Guger
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

package at.guger.moneybook.ui.home.budgets

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import at.guger.moneybook.data.model.BudgetWithBalance
import at.guger.moneybook.data.repository.BudgetsRepository
import java.time.LocalDate
import java.time.ZoneId

/**
 * [ViewModel] for the [BudgetsInfoBottomSheetDialogFragment].
 */
class BudgetsInfoViewModel(
    private val budgetsRepository: BudgetsRepository
) : ViewModel() {

    //region Variables

    //endregion

    //region Methods

    fun budgetsForTime(start: LocalDate, end: LocalDate): LiveData<List<BudgetWithBalance>> {
        return budgetsRepository.getObservableBudgetsWithBalanceInTimeSpan(
            start.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            end.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
    }

    //endregion
}