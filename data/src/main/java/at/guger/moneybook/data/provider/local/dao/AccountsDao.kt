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
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.data.model.Transaction

/**
 * [Dao] method for querying [accounts][Account].
 */
@Dao
internal interface AccountsDao {

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun get(id: Long): Account

    @Query("SELECT * FROM accounts")
    fun getAccounts(): LiveData<List<Account>>

    @Query(
        """
            SELECT accounts.*, 
            (SELECT SUM(CASE WHEN transactions.type = ${Transaction.TransactionType.EARNING} THEN transactions.value 
            ELSE -transactions.value END) FROM transactions WHERE transactions.account_id = accounts.id) AS balance
            FROM accounts
            LEFT JOIN transactions ON transactions.account_id = accounts.id
            GROUP BY accounts.id
        """
    )
    fun getAccountsWithBalance(): LiveData<List<AccountWithBalance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(vararg account: Account)
}