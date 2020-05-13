/*
 * Copyright 2019 Daniel Guger
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
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import at.guger.moneybook.data.Database
import at.guger.moneybook.data.model.base.Model
import kotlinx.android.parcel.Parcelize

/**
 * AppDatabase entity for budgets being part of a [Transaction].
 */
@Parcelize
@Entity(tableName = Database.Budgets.TABLE_NAME)
open class Budget(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Database.Budgets.COL_ID) var id: Long = 0,
    @ColumnInfo(name = Database.Budgets.COL_NAME) var name: String = "",
    @ColumnInfo(name = Database.Budgets.COL_BUDGET) var budget: Double = 0.0,
    @ColumnInfo(name = Database.Budgets.COL_COLOR) @ColorInt var color: Int = 0
) : Model