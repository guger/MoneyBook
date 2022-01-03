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

package at.guger.moneybook.data.provider.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import at.guger.moneybook.data.model.Transaction

/**
 * [Dao] method for querying [transactions][Transaction].
 */
@Dao
internal interface TransactionsDao {

    @androidx.room.Transaction
    @Query("SELECT transactions.* FROM transactions WHERE id = :id")
    suspend fun get(id: Long): Transaction

    @Query("SELECT transactions.* FROM transactions")
    suspend fun getEntities(): List<Transaction.TransactionEntity>

    @androidx.room.Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions 
        WHERE account_id = :accountId AND type IN (${Transaction.TransactionType.EARNING}, ${Transaction.TransactionType.EXPENSE})
        ORDER BY date DESC, title ASC
        """
    )
    fun getByAccount(accountId: Long): LiveData<List<Transaction>>

    @androidx.room.Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions 
        WHERE budget_id = :budgetId AND type IN (${Transaction.TransactionType.EARNING}, ${Transaction.TransactionType.EXPENSE})
        ORDER BY date DESC, title ASC
        """
    )
    fun getByBudget(budgetId: Long): LiveData<List<Transaction>>

    @androidx.room.Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions
        WHERE type IN (${Transaction.TransactionType.EARNING}, ${Transaction.TransactionType.EXPENSE})
        ORDER BY date DESC, title ASC
        """
    )
    fun getEarningsAndExpenses(): LiveData<List<Transaction>>

    @androidx.room.Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions
        WHERE type IN (${Transaction.TransactionType.CLAIM}, ${Transaction.TransactionType.DEBT})
        ORDER BY
            paid,
            CASE paid WHEN 1 THEN -1.0 * due ELSE due END,
            date DESC, 
            title
        """
    )
    fun getClaimsAndDebts(): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactionEntity: Transaction.TransactionEntity): Long

    @Update
    suspend fun update(transactionEntity: Transaction.TransactionEntity)

    @Delete
    suspend fun delete(vararg transactionEntity: Transaction.TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}