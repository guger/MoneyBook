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

package at.guger.moneybook.data.repository

import android.graphics.Color
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.guger.moneybook.data.BuildConfig
import at.guger.moneybook.data.base.DatabaseTest
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.data.util.observeOnce
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.LocalDate

/**
 * Test class for [TransactionsRepository].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class TransactionsRepositoryTest : DatabaseTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var transactionsRepository: TransactionsRepository
    private lateinit var accountsRepository: AccountsRepository
    private lateinit var budgetsRepository: BudgetsRepository

    @Before
    fun setUp() {
        transactionsRepository = TransactionsRepository(database)
        accountsRepository = AccountsRepository(database)
        budgetsRepository = BudgetsRepository(database)

        runBlocking {
            accountsRepository.insert(Account(ACCOUNT_ID, ACCOUNT_NAME))
            budgetsRepository.insert(Budget(BUDGET_ID, BUDGET_NAME, BUDGET_COLOR))
        }
    }

    @Test
    fun testInsert() = runBlocking {
        val id = transactionsRepository.insert(TRANSACTION)

        assertThat(id).isEqualTo(ID)

        transactionsRepository.getEarningsAndExpenses().observeOnce {
            assertThat(it).hasSize(1)
            assertTransaction(it.first())
        }
    }

    private fun assertTransaction(transaction: Transaction) {
        with(transaction) {
            assertThat(id).isEqualTo(ID)
            assertThat(title).isEqualTo(TITLE)
            assertThat(date).isEqualTo(DATE)
            assertThat(value).isEqualTo(VALUE)
            assertThat(notes).isEqualTo(NOTES)
            assertThat(type).isEqualTo(TYPE)
            assertThat(account?.id).isEqualTo(ACCOUNT_ID)
            assertThat(account?.name).isEqualTo(ACCOUNT_NAME)
            assertThat(budget?.id).isEqualTo(BUDGET_ID)
            assertThat(budget?.name).isEqualTo(BUDGET_NAME)
            assertThat(budget?.color).isEqualTo(BUDGET_COLOR)
            assertThat(contacts?.get(0)?.id).isEqualTo(CONTACT_ID)
            assertThat(contacts?.get(0)?.contactId).isEqualTo(CONTACT_CONTACT_ID)
            assertThat(contacts?.get(0)?.contactName).isEqualTo(CONTACT_NAME)
            assertThat(contacts?.get(0)?.transactionId).isEqualTo(CONTACT_TRANSACTION_ID)
            assertThat(contacts?.get(0)?.paidState).isEqualTo(CONTACT_PAID_STATE)
        }
    }

    companion object {
        private const val ID: Long = 13
        private const val TITLE: String = "Test Transaction"
        private val DATE: LocalDate = LocalDate.now()
        private const val VALUE: Double = 22.5
        private const val NOTES: String = "Test Notes"
        private const val TYPE: Int = Transaction.TransactionType.EXPENSE
        private const val ACCOUNT_ID: Long = 18
        private const val ACCOUNT_NAME: String = "Test Account"
        private const val BUDGET_ID: Long = 22
        private const val BUDGET_NAME: String = "Test Budget"
        private const val BUDGET_COLOR: Int = Color.BLUE
        private const val CONTACT_ID: Long = 1
        private const val CONTACT_CONTACT_ID: Long = 1515
        private const val CONTACT_NAME: String = "Test Contact"
        private const val CONTACT_TRANSACTION_ID: Long = ID
        private const val CONTACT_PAID_STATE: Int = Contact.PaidState.STATE_NOT_PAID

        val TRANSACTION = Transaction(
            Transaction.TransactionEntity(
                id = ID,
                title = TITLE,
                date = DATE,
                value = VALUE,
                notes = NOTES,
                type = TYPE,
                accountId = ACCOUNT_ID,
                budgetId = BUDGET_ID
            ),
            account = Account(ACCOUNT_ID, ACCOUNT_NAME),
            budget = Budget(BUDGET_ID, BUDGET_NAME, BUDGET_COLOR),
            contacts = listOf(Contact(CONTACT_ID, CONTACT_CONTACT_ID, CONTACT_NAME, CONTACT_TRANSACTION_ID, CONTACT_PAID_STATE))
        )
    }
}