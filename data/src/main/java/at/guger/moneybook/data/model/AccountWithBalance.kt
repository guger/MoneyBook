/*
 * Copyright 2020 Daniel Guger
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

import androidx.room.ColumnInfo
import at.guger.moneybook.data.Database
import kotlinx.android.parcel.Parcelize

/**
 * AppDatabase entity for accounts being part of a [Transaction].
 */
@Parcelize
data class AccountWithBalance(
    @ColumnInfo(name = Database.Accounts.COL_BALANCE) var balance: Double = 0.0
) : Account()