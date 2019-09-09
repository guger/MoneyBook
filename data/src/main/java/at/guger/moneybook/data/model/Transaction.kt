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
    @Relation(entity = Category::class, parentColumn = Database.Transactions.COL_CATEGORY_ID, entityColumn = Database.Categories.COL_ID) val category: Category? = null,
    @Relation(entity = Contact::class, parentColumn = Database.Transactions.COL_ID, entityColumn = Database.Contacts.COL_TRANSACTION_ID) val contacts: List<Contact>? = null
) : Model {

    val id: Long
        get() = entity.id

    val title: String
        get() = entity.title

    val date: LocalDate
        get() = entity.date

    val value: Double
        get() = entity.value

    val notes: String
        get() = entity.notes

    @TransactionType
    val type: Int
        get() = entity.type

    @Parcelize
    @Entity(tableName = Database.Transactions.TABLE_NAME)
    data class TransactionEntity(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Database.Transactions.COL_ID) val id: Long = 0L,
        @ColumnInfo(name = Database.Transactions.COL_TITLE) val title: String = "",
        @ColumnInfo(name = Database.Transactions.COL_DATE) val date: LocalDate = LocalDate.MIN,
        @ColumnInfo(name = Database.Transactions.COL_VALUE) val value: Double = 0.0,
        @ColumnInfo(name = Database.Transactions.COL_NOTES) val notes: String = "",
        @ColumnInfo(name = Database.Transactions.COL_TYPE) @TransactionType val type: Int = TransactionType.EARNING,
        @ColumnInfo(name = Database.Transactions.COL_ACCOUNT_ID) val accountId: Long = Account.DEFAULT_ACCOUNT_ID,
        @ColumnInfo(name = Database.Transactions.COL_CATEGORY_ID) val categoryId: Long? = null
    ) : Model

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