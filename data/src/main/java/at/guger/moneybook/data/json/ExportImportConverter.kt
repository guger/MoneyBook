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

package at.guger.moneybook.data.json

import at.guger.moneybook.data.model.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exporting/importing the database as JSON/from JSON.
 */
class ExportImportConverter {

    private val moshi: Moshi = Moshi.Builder()
        .add(LocalDateAdapter())
        .add(LocalDateTimeAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter: JsonAdapter<ExportImportModel> = moshi.adapter(ExportImportModel::class.java)

    suspend fun convertExport(export: ExportImportModel): String = withContext(Dispatchers.IO) {
        return@withContext adapter.toJson(export)
    }

    suspend fun convertImport(import: String): ExportImportModel = withContext(Dispatchers.IO) {
        return@withContext adapter.fromJson(import)!!
    }

    data class ExportImportModel(
        val transactionEntities: List<Transaction.TransactionEntity>,
        val accounts: List<Account>,
        val contacts: List<Contact>,
        val reminders: List<Reminder>,
        val budgets: List<Budget>
    )
}