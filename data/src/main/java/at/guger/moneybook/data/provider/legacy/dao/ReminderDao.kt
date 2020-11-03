package at.guger.moneybook.data.provider.legacy.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.BOOKENTRY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import at.guger.moneybook.data.provider.legacy.model.Reminder

/**
 * [Dao] for MoneyBook [Reminders][Reminder].
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Dao
interface ReminderDao {

    @Query("SELECT * FROM ${LegacyDatabase.Table.REMINDERS}")
    suspend fun getReminders(): List<Reminder>

    @Query("SELECT * FROM ${LegacyDatabase.Table.REMINDERS}")
    fun getObservableReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM ${LegacyDatabase.Table.REMINDERS} WHERE $ID = :id")
    suspend fun get(id: Long): Reminder?

    @Query("SELECT * FROM ${LegacyDatabase.Table.REMINDERS} WHERE $BOOKENTRY_ID = :bookEntryId")
    suspend fun getBookEntryReminder(bookEntryId: Long): Reminder?

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)
}