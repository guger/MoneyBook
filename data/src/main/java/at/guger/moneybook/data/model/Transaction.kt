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
import org.threeten.bp.LocalDate

/**
 * AppDatabase entity for transactions.
 */
@Parcelize
class Transaction(
    @Embedded val entity: TransactionEntity,
    @Relation(entity = Account::class, parentColumn = Database.Transactions.COL_ACCOUNT_ID, entityColumn = Database.Accounts.COL_ID) val account: Account? = null,
    @Relation(entity = Budget::class, parentColumn = Database.Transactions.COL_BUDGET_ID, entityColumn = Database.Budgets.COL_ID) val budget: Budget? = null,
    @Relation(entity = Contact::class, parentColumn = Database.Transactions.COL_ID, entityColumn = Database.Contacts.COL_TRANSACTION_ID) val contacts: List<Contact>? = null
) : Model {

    val id: Long
        get() = entity.id

    val title: String
        get() = entity.title

    val date: LocalDate
        get() = entity.date

    val due: LocalDate?
        get() = entity.due

    val value: Double
        get() = entity.value

    val notes: String
        get() = entity.notes

    @TransactionType
    val type: Int
        get() = entity.type

    override fun equals(other: Any?): Boolean {
        return other is Transaction && other.id == id && other.title == title && other.date == date &&
                other.value == value && other.due == due && other.notes == notes && other.type == type &&
                other.account == account && other.budget == budget && other.contacts == contacts
    }

    override fun hashCode(): Int {
        var result = entity.hashCode()
        result = 31 * result + (account?.hashCode() ?: 0)
        result = 31 * result + (budget?.hashCode() ?: 0)
        result = 31 * result + (contacts?.hashCode() ?: 0)
        return result
    }

    @Parcelize
    @Entity(
        tableName = Database.Transactions.TABLE_NAME,
        foreignKeys = [ForeignKey(entity = Account::class, parentColumns = [Database.Accounts.COL_ID], childColumns = [Database.Transactions.COL_ACCOUNT_ID], onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = [Database.Transactions.COL_ACCOUNT_ID])]
    )
    data class TransactionEntity(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Database.Transactions.COL_ID) val id: Long = 0L,
        @ColumnInfo(name = Database.Transactions.COL_TITLE) val title: String = "",
        @ColumnInfo(name = Database.Transactions.COL_DATE) val date: LocalDate = LocalDate.MIN,
        @ColumnInfo(name = Database.Transactions.COL_DUE) val due: LocalDate? = null,
        @ColumnInfo(name = Database.Transactions.COL_VALUE) val value: Double = 0.0,
        @ColumnInfo(name = Database.Transactions.COL_NOTES) val notes: String = "",
        @ColumnInfo(name = Database.Transactions.COL_TYPE) @TransactionType val type: Int = TransactionType.EARNING,
        @ColumnInfo(name = Database.Transactions.COL_ACCOUNT_ID) val accountId: Long? = null,
        @ColumnInfo(name = Database.Transactions.COL_BUDGET_ID) val budgetId: Long? = null
    ) : Model

    @Target(AnnotationTarget.TYPE, AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
    @IntDef(value = [TransactionType.EARNING, TransactionType.EXPENSE, TransactionType.CLAIM, TransactionType.DEBT])
    @Retention(AnnotationRetention.SOURCE)
    annotation class TransactionType {
        companion object {
            const val EARNING = 0
            const val EXPENSE = 1
            const val CLAIM = 2
            const val DEBT = 3
        }
    }
}