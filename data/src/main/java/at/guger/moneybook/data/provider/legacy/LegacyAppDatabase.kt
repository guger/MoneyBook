package at.guger.moneybook.data.provider.legacy

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.DATABASE_NAME
import at.guger.moneybook.data.provider.legacy.converter.DateConverter
import at.guger.moneybook.data.provider.legacy.dao.BookEntryDao
import at.guger.moneybook.data.provider.legacy.dao.CategoryDao
import at.guger.moneybook.data.provider.legacy.dao.ReminderDao
import at.guger.moneybook.data.provider.legacy.model.BookEntry
import at.guger.moneybook.data.provider.legacy.model.Category
import at.guger.moneybook.data.provider.legacy.model.Contact
import at.guger.moneybook.data.provider.legacy.model.Reminder

/**
 * [RoomDatabase] class for handling [BookEntries][BookEntry], [Categories][Category] and [Reminders][Reminder] data.
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Database(entities = [BookEntry.BookEntryEntity::class, Category::class, Contact.ContactEntity::class, Reminder::class], version = 5)
@TypeConverters(DateConverter::class)
abstract class LegacyAppDatabase : RoomDatabase() {

    abstract fun bookEntryDao(): BookEntryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao

    companion object {

        @Volatile
        private var INSTANCE: LegacyAppDatabase? = null

        fun get(context: Context): LegacyAppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, LegacyAppDatabase::class.java, DATABASE_NAME)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        //region Migrations

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }

        //endregion
    }
}