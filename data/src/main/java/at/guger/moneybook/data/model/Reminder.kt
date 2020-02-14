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

package at.guger.moneybook.data.model

import androidx.room.*
import at.guger.moneybook.data.Database
import at.guger.moneybook.data.Database.Reminders.COL_DATE
import at.guger.moneybook.data.Database.Reminders.COL_ID
import at.guger.moneybook.data.Database.Reminders.COL_TRANSACTION_ID
import at.guger.moneybook.data.provider.local.AppDatabase
import org.threeten.bp.LocalDate

/**
 * [AppDatabase] entity for reminders for [transactions][Transaction] of type [Transaction.TransactionType.CLAIM] or [Transaction.TransactionType.DEBT].
 */
@Entity(
    tableName = Database.Reminders.TABLE_NAME,
    foreignKeys = [ForeignKey(entity = Transaction.TransactionEntity::class, parentColumns = [Database.Transactions.COL_ID], childColumns = [COL_TRANSACTION_ID], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = [COL_TRANSACTION_ID], unique = true)]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COL_ID) val id: Long = 0,
    @ColumnInfo(name = COL_TRANSACTION_ID) val transactionId: Long = -1,
    @ColumnInfo(name = COL_DATE) val date: LocalDate = LocalDate.MIN
)