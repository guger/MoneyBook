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

import androidx.annotation.IntDef
import androidx.room.*
import at.guger.moneybook.data.Database
import at.guger.moneybook.data.model.base.Model
import kotlinx.android.parcel.Parcelize

/**
 * AppDatabase entry for contacts being part of a [Transaction].
 */
@Parcelize
@Entity(
    tableName = Database.Contacts.TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = Transaction.TransactionEntity::class,
        parentColumns = [Database.Transactions.COL_ID],
        childColumns = [Database.Contacts.COL_TRANSACTION_ID],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(Database.Contacts.COL_TRANSACTION_ID)]
)
data class Contact(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Database.Contacts.COL_ID) val id: Long,
    @ColumnInfo(name = Database.Contacts.COL_CONTACT_ID) val contactId: Long,
    @ColumnInfo(name = Database.Contacts.COL_CONTACT_NAME) val contactName: String,
    @ColumnInfo(name = Database.Contacts.COL_TRANSACTION_ID) val transactionId: Long,
    @ColumnInfo(name = Database.Contacts.COL_PAID_STATE) @PaidState val paidState: Int
) : Model {

    @IntDef(value = [PaidState.STATE_NOT_PAID, PaidState.STATE_PAID])
    annotation class PaidState {
        companion object {
            const val STATE_NOT_PAID = 0
            const val STATE_PAID = 1
        }
    }
}