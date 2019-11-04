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

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import at.guger.moneybook.data.base.DatabaseTest
import at.guger.moneybook.data.model.Contact
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


/**
 * Test class for [ContactsRepository].
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class ContactsRepositoryTest : DatabaseTest() {

    private lateinit var repository: ContactsRepository
    private lateinit var addressBookRepository: AddressBookRepository

    @Before
    fun setUp() {
        addressBookRepository = AddressBookRepository(InstrumentationRegistry.getInstrumentation().context.contentResolver)

        repository = ContactsRepository(database, addressBookRepository)
    }

    @Test
    fun testInsertContact() = runBlocking {
        assertThat(repository.findByTransactionId(TRANSACTION_ID)).isEmpty()

        repository.insert(listOf(CONTACT))

        assertThat(repository.findByTransactionId(TRANSACTION_ID)).hasSize(1)
        assertThat(repository.findByTransactionId(TRANSACTION_ID)).contains(CONTACT)
    }

    @Test
    fun testUpdateAccount() = runBlocking {
        repository.insert(listOf(CONTACT))

        assertThat(repository.findByTransactionId(TRANSACTION_ID)).contains(CONTACT)

        repository.update(listOf(CONTACT.copy(paidState = Contact.PaidState.STATE_PAID)))

        assertThat(repository.findByTransactionId(TRANSACTION_ID)).hasSize(1)
        assertThat(repository.findByTransactionId(TRANSACTION_ID)).contains(CONTACT.copy(paidState = Contact.PaidState.STATE_PAID))
    }

    @Test
    fun testDeleteAccount() = runBlocking {
        repository.insert(listOf(CONTACT))

        assertThat(repository.findByTransactionId(TRANSACTION_ID)).isNotEmpty()

        repository.delete(listOf(CONTACT))

        assertThat(repository.findByTransactionId(TRANSACTION_ID)).isEmpty()
    }

    companion object {
        private const val CONTACT_ID: Long = 11
        private const val CONTACT_CONTACT_ID: Long = 114
        private const val CONTACT_NAME: String = "Max Mouse"
        private const val TRANSACTION_ID: Long = 778
        @Contact.PaidState
        private const val PAID_STATE: Int = Contact.PaidState.STATE_NOT_PAID

        val CONTACT = Contact(CONTACT_ID, CONTACT_CONTACT_ID, CONTACT_NAME, TRANSACTION_ID, PAID_STATE)
    }
}