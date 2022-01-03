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

package at.guger.moneybook.data.repository

import androidx.lifecycle.LiveData
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.BudgetWithBalance
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.BudgetsDao
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repository class for handling [budgets][Budget].
 */
class BudgetsRepository(database: AppDatabase) {

    //region Variables

    private val budgetsDao: BudgetsDao = database.budgetsDao()

    //endregion

    //region Methods

    fun get(budgetId: Long): LiveData<Budget> = budgetsDao.get(budgetId)

    fun getObservableBudgets(): LiveData<List<Budget>> = budgetsDao.getObservableBudgets()

    fun getBudgetsWithBalance(): LiveData<List<BudgetWithBalance>> = budgetsDao.getObservableBudgetsWithBalance(getCurrentMonthStart(), getCurrentMonthEnd())

    fun getBudgetsWithBalanceTimeSpan(start: Long, end: Long): LiveData<List<BudgetWithBalance>> = budgetsDao.getObservableBudgetsWithBalance(start, end)

    suspend fun insert(budget: Budget): Long {
        return budgetsDao.insert(budget)
    }

    suspend fun insert(vararg budget: Budget) {
        budgetsDao.insert(*budget)
    }

    suspend fun update(budget: Budget) {
        budgetsDao.update(budget)
    }

    suspend fun delete(vararg budget: Budget) {
        budgetsDao.delete(*budget)
    }

    private fun getCurrentMonthStart() = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun getCurrentMonthEnd() = LocalDate.now().run { withDayOfMonth(this.lengthOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }

    //endregion
}