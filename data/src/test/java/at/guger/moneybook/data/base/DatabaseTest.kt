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

package at.guger.moneybook.data.base

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import at.guger.moneybook.data.provider.local.AppDatabase
import org.junit.After
import org.junit.Before

/**
 * Test class implementing the app's database.
 */
abstract class DatabaseTest {

    protected lateinit var database: AppDatabase

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun shutDownDatabase() {
        database.clearAllTables()
        database.close()
    }
}