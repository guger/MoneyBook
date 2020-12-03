package at.guger.moneybook.data.provider.legacy.dao

import androidx.room.*
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.BOOKENTRY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.CATEGORY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.COLOR
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ICON_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.NAME
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.PREFIX_CATEGORY
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Table.BOOKENTRIES
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Table.BOOKENTRYCONTACTS
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Table.CATEGORIES
import at.guger.moneybook.data.provider.legacy.model.BookEntry
import at.guger.moneybook.data.provider.legacy.model.Contact

/**
 * [Dao] for MoneyBook [BookEntries][BookEntry].
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Dao
interface BookEntryDao {

    @Transaction
    @Query(
        "SELECT $BOOKENTRIES.*," +
                " $CATEGORIES.$ID AS $PREFIX_CATEGORY$ID, $CATEGORIES.$NAME AS $PREFIX_CATEGORY$NAME, $CATEGORIES.$ICON_ID AS $PREFIX_CATEGORY$ICON_ID, $CATEGORIES.$COLOR AS $PREFIX_CATEGORY$COLOR" +
                " FROM $BOOKENTRIES" +
                " LEFT JOIN $CATEGORIES ON $BOOKENTRIES.$CATEGORY_ID = $CATEGORIES.$ID"
    )
    suspend fun getBookEntries(): List<BookEntry>

    @Insert
    suspend fun insert(bookEntryEntity: BookEntry.BookEntryEntity): Long

    @Update
    suspend fun update(bookEntryEntity: BookEntry.BookEntryEntity)

    @Delete
    suspend fun delete(bookEntryEntity: BookEntry.BookEntryEntity)

    @Insert
    suspend fun insert(embeddedContacts: List<Contact.ContactEntity>)

    @Update
    suspend fun update(embeddedContact: Contact.ContactEntity)

    @Query("DELETE FROM $BOOKENTRYCONTACTS WHERE $BOOKENTRY_ID = :bookEntryId")
    suspend fun deleteBookEntryContacts(bookEntryId: Long)

    @Transaction
    suspend fun insert(bookEntry: BookEntry): Long {
        val id = insert(bookEntry.entity)
        bookEntry.embeddedContacts?.onEach { it.bookEntryId = id }?.let {
            insert(it)
        }

        return id
    }

    @Transaction
    suspend fun update(bookEntry: BookEntry) {
        update(bookEntry.entity)

        deleteBookEntryContacts(bookEntry.id)
        bookEntry.embeddedContacts?.onEach { it.bookEntryId = bookEntry.id }?.let {
            insert(it)
        }
    }

    @Transaction
    suspend fun delete(bookEntry: BookEntry) {
        delete(bookEntry.entity)
    }

    companion object {
    }
}