/*
 * Copyright 2022 Daniel Guger
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

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Crypto {

    const val BYTEARRAY_LENGTH = 16
    const val ALGORITHM = "AES"
    const val TRANSFORMATION = "AES/CBC/PKCS5PADDING"

    fun getKeyFromPassword(password: String): SecretKey {
        val keyBytes = password.toByteArray()

        if (keyBytes.size != BYTEARRAY_LENGTH) throw IllegalArgumentException("Length of password must be $BYTEARRAY_LENGTH bytes.")

        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encrypt(key: SecretKey, dataToEncrypt: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(BYTEARRAY_LENGTH)))

        return cipher.doFinal(dataToEncrypt)
    }

    fun decrypt(key: SecretKey, dataToDecrypt: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(BYTEARRAY_LENGTH)))

        return cipher.doFinal(dataToDecrypt)
    }
}