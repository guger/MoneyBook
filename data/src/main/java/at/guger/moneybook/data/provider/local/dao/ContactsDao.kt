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

import androidx.room.*
import at.guger.moneybook.data.model.Contact

/**
 * [Dao] method for querying [contacts][Contact].
 */
@Dao
internal interface ContactsDao {

    @Query("SELECT * FROM contacts")
    suspend fun getContacts(): List<Contact>

    @Query("SELECT * FROM contacts WHERE transaction_id = :id")
    suspend fun findByTransactionId(id: Long): List<Contact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contacts: List<Contact>)

    @Update
    suspend fun update(contacts: List<Contact>)

    @Delete
    suspend fun delete(contacts: List<Contact>)
}