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

package at.guger.moneybook.data.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class CryptoTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun test_getKeyFromPassword_valid() {
        val password = "abcdefghijklmnop"

        val key = Crypto.getKeyFromPassword(password)

        assertThat(key.algorithm).isEqualTo(Crypto.ALGORITHM)
    }

    @Test
    fun test_getKeyFromPassword_invalid() {
        val password = "abcdefghijkl"

        assertThrows(IllegalArgumentException::class.java) {
            Crypto.getKeyFromPassword(password)
        }
    }

    @Test
    fun test_encrypt_decrypt() {
        val password = "abcdefghijklmnop"

        val data = "This is a very secret number: 123, and some random umlauts: äöü!".toByteArray()

        val key = Crypto.getKeyFromPassword(password)

        val encryptedData = Crypto.encrypt(key, data)

        val decryptedData = Crypto.decrypt(key, encryptedData)

        assertThat(String(decryptedData, Charsets.UTF_8)).isEqualTo(String(data, Charsets.UTF_8))
    }
}