package at.guger.moneybook.data.provider.legacy.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.CATEGORY_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Table.BOOKENTRIES
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Table.CATEGORIES
import at.guger.moneybook.data.provider.legacy.model.Category

/**
 * [Dao] for MoneyBook [Categories][Category].
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM $CATEGORIES")
    suspend fun getCategories(): List<Category>

    @Query("SELECT * FROM $CATEGORIES")
    fun getObservableCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM $CATEGORIES WHERE $ID = :id")
    suspend fun get(id: Long): Category?

    @Query("SELECT count(*) FROM $BOOKENTRIES WHERE $CATEGORY_ID = :categoryId")
    suspend fun countCategoryUsage(categoryId: Long): Long

    @Insert
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultCategories(categories: Array<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}