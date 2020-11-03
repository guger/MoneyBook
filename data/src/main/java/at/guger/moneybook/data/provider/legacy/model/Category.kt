package at.guger.moneybook.data.provider.legacy.model

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.guger.moneybook.data.provider.legacy.LegacyDatabase
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.COLOR
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ICON_ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.ID
import at.guger.moneybook.data.provider.legacy.LegacyDatabase.Column.NAME
import kotlinx.android.parcel.Parcelize

/**
 * Model class for [Categories][Category].
 *
 * @author Daniel Guger
 * @version 1.0
 */
@Parcelize
@Entity(tableName = LegacyDatabase.Table.CATEGORIES)
data class Category(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID) val id: Long = 0,
    @ColumnInfo(name = NAME) val name: String = "",
    @ColumnInfo(name = ICON_ID) val iconId: Int = 0,
    @ColumnInfo(name = COLOR) @ColorInt val color: Int = Color.BLUE
) : Parcelable