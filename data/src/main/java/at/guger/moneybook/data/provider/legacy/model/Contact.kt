package at.guger.moneybook.data.provider.legacy.model

import android.os.Parcelable
import androidx.room.*
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.BOOKENTRY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.CONTACT_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.HAS_PAID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import kotlinx.parcelize.Parcelize

/**
 * Contact model class for [BookEntries][BookEntry].
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Parcelize
data class Contact(
    val id: Long,
    val bookEntryId: Long,
    val contactId: Long,
    val name: String,
    val hasPaid: Boolean
) : Parcelable {

    /**
     * Contact [Entity] in [RoomDatabase][androidx.room.RoomDatabase].
     *
     * @author Daniel Guger
     * @version 1.0
     */
    @Parcelize
    @Entity(
        tableName = LegacyDatabase.Table.BOOKENTRYCONTACTS,
        foreignKeys = [ForeignKey(entity = BookEntry.BookEntryEntity::class, parentColumns = [ID], childColumns = [BOOKENTRY_ID], onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = [BOOKENTRY_ID])]
    )
    data class ContactEntity(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID) var id: Long = 0,
        @ColumnInfo(name = BOOKENTRY_ID) var bookEntryId: Long = 0,
        @ColumnInfo(name = CONTACT_ID) val contactId: Long = -1,
        @ColumnInfo(name = HAS_PAID) var hasPaid: Boolean = false
    ) : Parcelable
}