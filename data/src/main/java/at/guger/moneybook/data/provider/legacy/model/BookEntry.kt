package at.guger.moneybook.data.provider.legacy.model

import android.os.Parcelable
import androidx.room.*
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.BOOKENTRY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.CATEGORY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.DATE
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ENTRYTYPE
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.IS_PAID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.NOTES
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.PREFIX_CATEGORY
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.TITLE
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.VALUE
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

/**
 * Model class for earnings, expenses, claims and debts.
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Parcelize
class BookEntry(
    @Embedded var entity: BookEntryEntity = BookEntryEntity(),
    @Embedded(prefix = PREFIX_CATEGORY) var category: Category? = null,
    @Relation(entity = Contact.ContactEntity::class, parentColumn = ID, entityColumn = BOOKENTRY_ID) var embeddedContacts: List<Contact.ContactEntity>? = null
) : Parcelable {

    var id: Long
        get() = entity.id
        set(value) {
            entity.id = value
        }

    val title: String
        get() = entity.title

    val date: LocalDate
        get() = entity.date

    val value: Double
        get() = entity.value

    val isPaid: Boolean
        get() = entity.isPaid

    val notes: String
        get() = entity.notes

    var entryType: Int
        get() = entity.entryType
        set(value) {
            entity.entryType = value
        }

    val isClaimOrDebt: Boolean
        get() = entryType == Type.Claim || entryType == Type.Debt

    fun copy(
        id: Long = this.id,
        title: String = this.title,
        date: LocalDate = this.date,
        value: Double = this.value,
        isPaid: Boolean = this.isPaid,
        notes: String = this.notes,
        entryType: Int = this.entryType,
        categoryId: Long? = this.category?.id,
        contacts: List<Contact.ContactEntity>? = this.embeddedContacts
    ) = create(id, title, date, value, isPaid, notes, entryType, categoryId, contacts)

    @Parcelize
    @Entity(tableName = LegacyDatabase.Table.BOOKENTRIES)
    data class BookEntryEntity(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID) var id: Long = 0,
        @ColumnInfo(name = TITLE) val title: String = "",
        @ColumnInfo(name = DATE) val date: LocalDate = LocalDate.MIN,
        @ColumnInfo(name = VALUE) val value: Double = 0.0,
        @ColumnInfo(name = IS_PAID) val isPaid: Boolean = false,
        @ColumnInfo(name = NOTES) val notes: String = "",
        @ColumnInfo(name = ENTRYTYPE) var entryType: Int = Type.Earning,
        @ColumnInfo(name = CATEGORY_ID) val categoryId: Long? = null
    ) : Parcelable

    object Type {
        const val Earning = 0
        const val Expense = 1
        const val Claim = 2
        const val Debt = 3
    }

    companion object {

        fun create(
            id: Long = 0,
            title: String,
            date: LocalDate,
            value: Double,
            isPaid: Boolean = false,
            notes: String,
            entryType: Int,
            categoryId: Long? = null,
            contacts: List<Contact.ContactEntity>? = null
        ): BookEntry {
            return BookEntry(
                BookEntryEntity(id = id, title = title, date = date, value = value, isPaid = isPaid, notes = notes, entryType = entryType, categoryId = categoryId),
                embeddedContacts = contacts
            )
        }

        fun createClaimEntry(
            title: String,
            date: LocalDate,
            value: Double,
            isPaid: Boolean = false,
            notes: String,
            categoryId: Long? = null,
            contacts: List<Contact.ContactEntity>? = null
        ): BookEntry {
            return create(title = title, date = date, value = value, isPaid = isPaid, notes = notes, entryType = Type.Claim, categoryId = categoryId, contacts = contacts)
        }

        fun createDebtEntry(
            title: String,
            date: LocalDate,
            value: Double,
            isPaid: Boolean = false,
            notes: String,
            categoryId: Long? = null,
            contacts: List<Contact.ContactEntity>? = null
        ): BookEntry {
            return create(title = title, date = date, value = value, isPaid = isPaid, notes = notes, entryType = Type.Debt, categoryId = categoryId, contacts = contacts)
        }
    }
}