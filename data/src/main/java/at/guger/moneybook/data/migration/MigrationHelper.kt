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

package at.guger.moneybook.data.migration

import android.content.Context
import java.io.File

/**
 * Migration helper for converting v1 data to v2.
 *
 * This package will be deleted when all users moved to v2.
 */
class MigrationHelper(context: Context) {


    @Suppress("UNREACHABLE_CODE")
    fun start(finished: () -> Unit) {
        TODO()

        finished()
    }

    companion object {
        const val LegacyDB = "MoneyBookDB.db"

        fun needMigration() = File(LegacyDB).exists()
    }
}