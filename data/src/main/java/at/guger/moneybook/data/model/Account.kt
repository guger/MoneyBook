/*
 * Copyright 2021 Daniel Guger
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

import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.guger.moneybook.data.Database
import at.guger.moneybook.data.model.base.Model
import kotlinx.parcelize.Parcelize

/**
 * AppDatabase entity for accounts being part of a [Transaction].
 */
@Keep
@Parcelize
@Entity(tableName = Database.Accounts.TABLE_NAME)
open class Account(
    @JvmField @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Database.Accounts.COL_ID) var id: Long = 0,
    @JvmField @ColumnInfo(name = Database.Accounts.COL_NAME) var name: String = "",
    @JvmField @ColumnInfo(name = Database.Accounts.COL_COLOR) @ColorInt var color: Int = -1,
    @JvmField @ColumnInfo(name = Database.Accounts.COL_START_BALANCE) var startBalance: Double = 0.0
) : Model {

    companion object {
        const val DEFAULT_ACCOUNT_ID = -100L
    }
}