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

package at.guger.moneybook.data.repository

import androidx.lifecycle.LiveData
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.AccountsDao

/**
 * Repository class for handling [accounts][Account].
 */
class AccountsRepository(database: AppDatabase) {

    //region Variables

    private val accountsDao: AccountsDao = database.accountsDao()

    //endregion

    //region Methods

    suspend fun get(id: Long): Account {
        return accountsDao.get(id)
    }

    suspend fun getAccounts(): List<Account> {
        return accountsDao.getAccounts()
    }

    fun getObservableAccounts(): LiveData<List<Account>> {
        return accountsDao.getObservableAccounts()
    }

    fun getObservableAccountsWithBalance(): LiveData<List<AccountWithBalance>> {
        return accountsDao.getObservableAccountsWithBalance()
    }

    suspend fun countAccounts(): Int {
        return accountsDao.countAccounts()
    }

    suspend fun insert(vararg account: Account) {
        accountsDao.insert(*account)
    }

    suspend fun update(account: Account) {
        accountsDao.update(account)
    }

    suspend fun delete(vararg account: Account) {
        accountsDao.delete(*account)
    }

    //endregion
}