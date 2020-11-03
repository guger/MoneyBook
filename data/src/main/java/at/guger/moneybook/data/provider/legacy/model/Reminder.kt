package at.guger.moneybook.data.provider.legacy.model

import androidx.room.*
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.BOOKENTRY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.FIREDATE
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import java.time.LocalDateTime

/**
 * Model class for claims.
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Entity(
    tableName = LegacyDatabase.Table.REMINDERS,
    foreignKeys = [ForeignKey(entity = BookEntry.BookEntryEntity::class, parentColumns = [ID], childColumns = [BOOKENTRY_ID], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = [BOOKENTRY_ID], unique = true)]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID) val id: Long = 0,
    @ColumnInfo(name = BOOKENTRY_ID) val bookEntryId: Long = -1,
    @ColumnInfo(name = FIREDATE) var fireDate: LocalDateTime = LocalDateTime.MIN
)