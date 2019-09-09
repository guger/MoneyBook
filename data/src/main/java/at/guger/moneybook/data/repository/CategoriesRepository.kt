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

import androidx.lifecycle.LiveData
import at.guger.moneybook.data.model.Category
import at.guger.moneybook.data.provider.local.AppDatabase
import at.guger.moneybook.data.provider.local.dao.CategoriesDao

/**
 * Repository class for handling [categories][Category].
 */
class CategoriesRepository(database: AppDatabase) {

    //region Variables

    private val categoriesDao: CategoriesDao = database.categoriesDao()

    //endregion

    //region Methods

    fun getObservableCategories(): LiveData<List<Category>> = categoriesDao.getObservableCategories()

    suspend fun insert(category: Category) {
        categoriesDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoriesDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoriesDao.delete(category)
    }

    //endregion
}