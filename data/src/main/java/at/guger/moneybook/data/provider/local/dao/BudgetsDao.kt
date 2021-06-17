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

package at.guger.moneybook.data.provider.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.BudgetWithBalance

/**
 * [Dao] method for querying [budgets][Budget].
 */
@Dao
internal interface BudgetsDao {

    @Query("SELECT * FROM budgets WHERE budgets.id = :id")
    fun get(id: Long): LiveData<Budget>

    @Query("SELECT * FROM budgets ORDER BY name ASC")
    suspend fun getBudgets(): List<Budget>

    @Query("SELECT * FROM budgets ORDER BY name ASC")
    fun getObservableBudgets(): LiveData<List<Budget>>

    @Query(
        """
            SELECT budgets.*, 
            (SELECT SUM(transactions.value) FROM transactions WHERE transactions.budget_id = budgets.id AND transactions.date >= :startDate AND transactions.date <= :endDate) AS balance
            FROM budgets
            LEFT JOIN transactions ON transactions.budget_id = budgets.id
            GROUP BY budgets.id
        """
    )
    fun getObservableBudgetsWithBalance(startDate: Long, endDate: Long): LiveData<List<BudgetWithBalance>>

    @Insert
    suspend fun insert(budget: Budget): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg budget: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(vararg budget: Budget)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}