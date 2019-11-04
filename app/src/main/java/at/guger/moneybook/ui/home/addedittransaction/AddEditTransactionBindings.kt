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

package at.guger.moneybook.ui.home.addedittransaction

import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import at.guger.moneybook.R
import at.guger.moneybook.data.model.Transaction
import com.google.android.material.button.MaterialButtonToggleGroup
import com.hootsuite.nachos.NachoTextView

/**
 * Binding adapters for the add/edit transaction dialog.
 */

@BindingAdapter("type", requireAll = true)
fun MaterialButtonToggleGroup.setType(@Transaction.TransactionType type: Int) {
    val buttonId = when (type) {
        Transaction.TransactionType.EARNING -> R.id.btnAddEditTransactionTypeEarning
        Transaction.TransactionType.EXPENSE -> R.id.btnAddEditTransactionTypeExpense
        Transaction.TransactionType.CLAIM -> R.id.btnAddEditTransactionTypeClaim
        else -> R.id.btnAddEditTransactionTypeDebt
    }

    check(buttonId)
}

@BindingAdapter("contacts", requireAll = true)
fun NachoTextView.setContacts(contacts: List<String>?) {
    setText(contacts)
}