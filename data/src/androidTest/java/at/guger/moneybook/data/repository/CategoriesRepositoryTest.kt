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
import at.guger.moneybook.data.base.DatabaseTest
import at.guger.moneybook.data.model.Category
import at.guger.moneybook.data.model.Contact
import at.guger.moneybook.data.util.observeOnce
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Test class for [CategoriesRepository].
 */
class CategoriesRepositoryTest : DatabaseTest() {

    private lateinit var repository: CategoriesRepository

    @Before
    fun setUp() {
        repository = CategoriesRepository(database)
    }

    @Test
    fun testInsertContact() = runBlocking {
        repository.insert(CATEGORY)

        repository.getObservableCategories().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(CATEGORY)
        }
    }

    @Test
    fun testUpdateAccount() = runBlocking {
        repository.insert(CATEGORY)

        repository.getObservableCategories().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(CATEGORY)
        }

        repository.update(CATEGORY.copy(color = Color.RED))

        repository.getObservableCategories().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(CATEGORY.copy(color = Color.RED))
        }
    }

    @Test
    fun testDeleteAccount() = runBlocking {
        repository.insert(CATEGORY)

        repository.getObservableCategories().observeOnce {
            assertThat(it).hasSize(1)
            assertThat(it).contains(CATEGORY)
        }
        repository.delete(CATEGORY)

        repository.getObservableCategories().observeOnce {
            assertThat(it).isEmpty()
        }
    }

    companion object {
        private const val CATEGORY_ID: Long = 22
        private const val CATEGORY_NAME: String = "Test Category"
        private const val CATEGORY_ICON: Int = 112
        private const val CATEGORY_COLOR: Int = Color.BLUE

        val CATEGORY = Category(CATEGORY_ID, CATEGORY_NAME, CATEGORY_ICON, CATEGORY_COLOR)
    }
}