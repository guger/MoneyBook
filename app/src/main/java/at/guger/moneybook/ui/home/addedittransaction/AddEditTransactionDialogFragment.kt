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

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.forEach
import androidx.core.view.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.FullScreenDialogFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.toEpochMilli
import at.guger.moneybook.core.util.toLocalDate
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.DialogFragmentAddEditTransactionBinding
import at.guger.moneybook.util.BottomAppBarCutCornersTopEdge
import at.guger.moneybook.util.Utils
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.maltaisn.calcdialog.CalcDialog
import kotlinx.android.synthetic.main.dialog_fragment_add_edit_transaction.*
import org.jetbrains.anko.find
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * Dialog fragment for creating a new [transactions][Transaction].
 */
class AddEditTransactionDialogFragment : FullScreenDialogFragment(), CalcDialog.CalcDialogCallback {

    //region Variables

    private val args: AddEditTransactionDialogFragmentArgs by navArgs()

    private val viewModel: AddEditTransactionDialogFragmentViewModel by viewModel()

    //endregion

    //region DialogFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<DialogFragmentAddEditTransactionBinding>(inflater, R.layout.dialog_fragment_add_edit_transaction, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.transaction?.let { viewModel.setupTransaction(it) }

        setupLayout()
        setupEvents()
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        TooltipCompat.setTooltipText(fabAddEditTransactionSave, getString(R.string.Save))
        TooltipCompat.setTooltipText(btnAddEditTransactionChooseDate, getString(R.string.ChooseDate))
        TooltipCompat.setTooltipText(btnAddEditTransactionOpenCalculator, getString(R.string.OpenCalculator))

        edtAddEditTransactionAccount.inputType = InputType.TYPE_NULL
        edtAddEditTransactionBudget.inputType = InputType.TYPE_NULL

        mAddEditTransactionTypeToggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            var hasChecked = false

            group.forEach { if ((it as MaterialButton).isChecked) hasChecked = true }

            if (!isChecked && !hasChecked) group.find<MaterialButton>(checkedId).isChecked = true

            if (isChecked) viewModel.onTransactionTypeChanged(checkedId)
        }

        btnAddEditTransactionChooseDate.setOnClickListener { showDatePicker() }
        btnAddEditTransactionOpenCalculator.setOnClickListener { showCalculator() }

        val bottomAppBarBackground: MaterialShapeDrawable = mBottomAppBar.background as MaterialShapeDrawable
        bottomAppBarBackground.shapeAppearanceModel = bottomAppBarBackground.shapeAppearanceModel.toBuilder().setTopEdge(
            BottomAppBarCutCornersTopEdge(
                mBottomAppBar.fabCradleMargin,
                mBottomAppBar.fabCradleRoundedCornerRadius,
                mBottomAppBar.cradleVerticalOffset
            )
        ).build()
        mBottomAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupEvents() {
        viewModel.accounts.observe(viewLifecycleOwner, Observer { accounts ->
            edtAddEditTransactionAccount.setAdapter(ArrayAdapter<String>(requireContext(), R.layout.dropdown_layout_popup_item, accounts.map { it.name }))
            edtAddEditTransactionAccount.setText(accounts.first().name, false)
        })
        viewModel.budgets.observe(viewLifecycleOwner, Observer { budgets ->
            val budgetEntries = budgets.map { it.name }.toMutableList().apply { add(0, "") }
            edtAddEditTransactionBudget.setAdapter(ArrayAdapter<String>(requireContext(), R.layout.dropdown_layout_popup_item, budgetEntries))
        })

        viewModel.showDatePicker.observe(viewLifecycleOwner, EventObserver { showDatePicker() })
        viewModel.showCalculator.observe(viewLifecycleOwner, EventObserver { showCalculator() })

        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(mBottomAppBar, it, Snackbar.LENGTH_LONG)
                .setAnchorView(fabAddEditTransactionSave)
                .show()
        })

        viewModel.transactionSaved.observe(viewLifecycleOwner, Observer { dismiss() })
    }

    private fun showDatePicker() {
        val today = LocalDate.now().toEpochMilli()

        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.ChooseDate)
            .setSelection(today)
            .build()

        datePickerDialog.addOnPositiveButtonClickListener {
            viewModel.transactionDate.value = it.toLocalDate().format(Utils.MEDIUM_DATE_FORMAT)
            edtAddEditTransactionDate.postDelayed(25) { edtAddEditTransactionDate.apply { setSelection(text?.length ?: 0) } }
        }

        datePickerDialog.show(childFragmentManager, null)
    }

    private fun showCalculator() {
        CalcDialog().apply {
            settings.apply {
                numberFormat = DecimalFormat.getNumberInstance().apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
                initialValue = this@AddEditTransactionDialogFragment.edtAddEditTransactionValue.getDecimalNumber().toBigDecimal()
                minValue = 0.toBigDecimal()
                isSignBtnShown = false
            }
        }.show(childFragmentManager, null)
    }

    //endregion

    //region Callback

    override fun onValueEntered(requestCode: Int, value: BigDecimal?) {
        viewModel.transactionValue.value = value?.toDouble()?.let { CurrencyTextInputEditText.CURRENCY_FORMAT.format(it) }
        edtAddEditTransactionValue.postDelayed(25) { edtAddEditTransactionValue.apply { setSelection(text?.length ?: 0) } }
    }

    //endregion
}