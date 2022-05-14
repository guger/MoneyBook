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

package at.guger.moneybook.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import at.guger.moneybook.data.Database
import at.guger.moneybook.data.model.base.Model
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = Database.Budgets.TABLE_NAME)
data class TransactionSuggestion(
    @ColumnInfo(name = Database.Transactions.COL_TITLE) val title: String = "",
    @ColumnInfo(name = Database.Transactions.COL_VALUE) val value: Double = 0.0,
    @ColumnInfo(name = Database.Transactions.COL_TYPE) @Transaction.TransactionType val type: Int = Transaction.TransactionType.EARNING,
    @ColumnInfo(name = "accountName") var accountName: String? = null,
    @ColumnInfo(name = "budgetName") var budgetName: String? = null,
) : Model
