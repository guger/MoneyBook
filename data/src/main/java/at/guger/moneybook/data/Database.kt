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

package at.guger.moneybook.data

/**
 * AppDatabase, table and column names.
 */
object Database {

    const val DATABASE_NAME = "MoneyBook.db"
    const val DATABASE_VERSION = 1

    const val ID = "id"

    object Transactions {
        const val TABLE_NAME = "transactions"

        const val COL_ID = ID
        const val COL_TITLE = "title"
        const val COL_DATE = "date"
        const val COL_DUE = "due"
        const val COL_VALUE = "value"
        const val COL_NOTES = "notes"
        const val COL_TYPE = "type"
        const val COL_ACCOUNT_ID = "account_id"
        const val COL_BUDGET_ID = "budget_id"
    }

    object Budgets {
        const val TABLE_NAME = "budgets"

        const val COL_ID = ID
        const val COL_NAME = "name"
        const val COL_COLOR = "color"
        const val COL_RECURRENCE = "recurrence"
    }

    object Contacts {
        const val TABLE_NAME = "contacts"

        const val COL_ID = ID
        const val COL_CONTACT_ID = "contact_id"
        const val COL_CONTACT_NAME = "contact_name"
        const val COL_TRANSACTION_ID = "transaction_id"
        const val COL_PAID_STATE = "paid_state"
    }

    object Accounts {
        const val TABLE_NAME = "accounts"

        const val COL_ID = ID
        const val COL_NAME = "name"
        const val COL_BALANCE = "balance"
    }
}