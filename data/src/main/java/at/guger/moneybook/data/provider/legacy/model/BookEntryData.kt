package at.guger.moneybook.data.provider.legacy.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.CONTACTS_COUNT
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.DATE
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ENTRYTYPE
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.PREFIX_CATEGORY
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.VALUE
import java.time.LocalDate
import kotlin.math.max

/**
 * Compact model class for [BookEntries][BookEntry].
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Entity(tableName = LegacyDatabase.Table.BOOKENTRIES)
data class BookEntryData(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID) val id: Long = 0,
    @ColumnInfo(name = DATE) val date: LocalDate = LocalDate.MIN,
    @ColumnInfo(name = VALUE) val value: Double = 0.0,
    @ColumnInfo(name = ENTRYTYPE) val entryType: Int = BookEntry.Type.Earning,
    @ColumnInfo(name = CONTACTS_COUNT) val contactsCount: Int = 0,
    @Embedded(prefix = PREFIX_CATEGORY) val category: Category? = null
) {
    val summedValue: Double
        get() = value * max(contactsCount, 1)
}