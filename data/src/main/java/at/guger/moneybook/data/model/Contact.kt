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
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Database.Contacts.COL_ID) val id: Long = 0,
    @ColumnInfo(name = Database.Contacts.COL_CONTACT_ID) val contactId: Long = -1,
    @ColumnInfo(name = Database.Contacts.COL_CONTACT_NAME) val contactName: String = "",
    @ColumnInfo(name = Database.Contacts.COL_TRANSACTION_ID) var transactionId: Long = -1,
    @ColumnInfo(name = Database.Contacts.COL_PAID_STATE) @PaidState val paidState: Int = PaidState.STATE_NOT_PAID
) : Model {

    override fun equals(other: Any?): Boolean {
        return other is Contact && contactId == other.contactId && contactName == other.contactName && transactionId == other.transactionId && paidState == other.paidState
    }

    override fun hashCode(): Int {
        var result = contactId.hashCode()
        result = 31 * result + contactName.hashCode()
        result = 31 * result + transactionId.hashCode()
        result = 31 * result + paidState
        return result
    }

    @IntDef(value = [PaidState.STATE_NOT_PAID, PaidState.STATE_PAID])
    annotation class PaidState {
        companion object {
            const val STATE_NOT_PAID = 0
            const val STATE_PAID = 1
        }
    }
}