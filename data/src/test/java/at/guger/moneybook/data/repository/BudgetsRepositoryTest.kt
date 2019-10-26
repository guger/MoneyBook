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
import androidx.test.ext.junit.runners.AndroidJUnit4
import at.guger.moneybook.data.base.DatabaseTest
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.util.observeOnce
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class for [BudgetsRepository].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class BudgetsRepositoryTest : DatabaseTest() {

    private lateinit var repository: BudgetsRepository

    @Before
    fun setUp() {
        repository = BudgetsRepository(database)
    }

    @Test
    fun testInsertContact() = runBlocking {
        repository.insert(BUDGET)

        repository.getBudgets().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(BUDGET)
        }
    }

    @Test
    fun testUpdateAccount() = runBlocking {
        repository.insert(BUDGET)

        repository.getBudgets().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(BUDGET)
        }

        repository.update(BUDGET.copy(color = Color.RED))

        repository.getBudgets().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(BUDGET.copy(color = Color.RED))
        }
    }

    @Test
    fun testDeleteAccount() = runBlocking {
        repository.insert(BUDGET)

        repository.getBudgets().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(BUDGET)
        }
        repository.delete(BUDGET)

        repository.getBudgets().observeOnce {
            assertThat(it).isEmpty()
        }
    }

    companion object {
        private const val BUDGET_ID: Long = 22
        private const val BUDGET_NAME: String = "Test Budget"
        private const val BUDGET_COLOR: Int = Color.BLUE

        val BUDGET = Budget(BUDGET_ID, BUDGET_NAME, BUDGET_COLOR)
    }
}