/*
 * Copyright 2020 Daniel Guger
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

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.guger.moneybook.data.base.DatabaseTest
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.util.observeOnce
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Test class for [AccountsRepository].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AccountsRepositoryTest : DatabaseTest() {

    private lateinit var repository: AccountsRepository

    @Before
    fun setUp() {
        repository = AccountsRepository(database)
    }

    @Test
    fun testInsertAccount() = runBlocking {
        repository.insert(ACCOUNT)

        repository.getObservableAccounts().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(ACCOUNT)
        }
    }

    @Test
    fun testUpdateAccount() = runBlocking {
        repository.insert(ACCOUNT)

        repository.getObservableAccounts().observeOnce { assertThat(it).contains(ACCOUNT) }

        repository.update(Account(id = ACCOUNT.id, name = UPDATED_ACCOUNT_NAME))

        repository.getObservableAccounts().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(Account(id = ACCOUNT.id, name = UPDATED_ACCOUNT_NAME))
        }
    }

    @Test
    fun testDeleteAccount() = runBlocking {
        repository.insert(ACCOUNT)

        repository.getObservableAccounts().observeOnce { assertThat(it).isNotEmpty() }

        repository.delete(ACCOUNT)

        repository.getObservableAccounts().observeOnce { assertThat(it).isEmpty() }
    }

    companion object {
        private const val ACCOUNT_ID: Long = 18
        private const val ACCOUNT_NAME: String = "Test Account"
        private const val UPDATED_ACCOUNT_NAME: String = "Updated Test Account"

        val ACCOUNT = Account(ACCOUNT_ID, ACCOUNT_NAME)
    }
}