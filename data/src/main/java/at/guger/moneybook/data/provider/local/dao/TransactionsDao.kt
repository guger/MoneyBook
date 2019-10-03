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

    @androidx.room.Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions WHERE account_id = :accountId
        ORDER BY date DESC, title ASC
        """
    )
    fun getByAccount(accountId: Long): LiveData<List<Transaction>>

    @androidx.room.Transaction
    @Query(
        """
        SELECT transactions.* FROM transactions
        ORDER BY date DESC, title ASC
        """
    )
    fun getTransactions(): LiveData<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactionEntity: Transaction.TransactionEntity): Long

    @Update
    suspend fun update(transactionEntity: Transaction.TransactionEntity)

    @Delete
    suspend fun delete(vararg transactionEntity: Transaction.TransactionEntity)
}