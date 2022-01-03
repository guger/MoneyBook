/*
 * Copyright 2022 Daniel Guger
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
import at.guger.moneybook.data.Database
import kotlinx.parcelize.Parcelize

/**
 * AppDatabase entity for budgets being part of a [Transaction].
 */
@Parcelize
data class BudgetWithBalance(
    @ColumnInfo(name = Database.Budgets.COL_BALANCE) val balance: Double
) : Budget() {

    constructor(id: Long, name: String, balance: Double, budget: Double, @ColorInt color: Int) : this(balance) {
        this.id = id
        this.name = name
        this.budget = budget
        this.color = color
    }
}