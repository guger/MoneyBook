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

package at.guger.moneybook.ui.home.addedittransaction

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.postDelayed
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingFragment
import at.guger.moneybook.core.ui.shape.BottomAppBarCutCornersTopEdge
import at.guger.moneybook.core.ui.transition.MaterialContainerTransition
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.ext.hasPermission
import at.guger.moneybook.core.util.ext.toLocalDate
import at.guger.moneybook.core.util.ext.utcMillis
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentAddEditTransactionBinding
import at.guger.moneybook.util.DateFormatUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.maltaisn.calcdialog.CalcDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate

/**
 * Dialog fragment for adding/editing a [transaction][Transaction].
 */
class AddEditTransactionFragment : BaseDataBindingFragment<FragmentAddEditTransactionBinding, AddEditTransactionViewModel>(), CalcDialog.CalcDialogCallback {

    //region Variables

    private val args by navArgs<AddEditTransactionFragmentArgs>()

    override val fragmentViewModel: AddEditTransactionViewModel by viewModel()

    //endregion

    //region Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): FragmentAddEditTransactionBinding {
        return FragmentAddEditTransactionBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        check(args.transaction == null || args.account == null) { "Cannot setup with account and transaction!" }

        // TODO: Request if permission isn't granted
        if (requireContext().hasPermission(Manifest.permission.READ_CONTACTS)) fragmentViewModel.loadContacts()

        setupLayout()
        setupEvents()

        args.transaction?.let { fragmentViewModel.setupTransaction(it) }
        args.account?.let { fragmentViewModel.setupAccount(it) }

        startTransition()

        binding.edtAddEditTransactionTitle.requestFocus()
        if (args.transaction != null) Handler(Looper.getMainLooper()).postDelayed({ binding.edtAddEditTransactionTitle.setSelection(binding.edtAddEditTransactionTitle.text?.length ?: 0) }, 200)
    }

    override fun onPause() {
        super.onPause()

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        with(binding) {
            TooltipCompat.setTooltipText(fabAddEditTransactionSave, getString(R.string.Save))

            edtAddEditTransactionAccount.inputType = InputType.TYPE_NULL
            edtAddEditTransactionBudget.inputType = InputType.TYPE_NULL

            mAddEditTransactionTypeToggleButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) fragmentViewModel.onTransactionTypeChanged(checkedId)
            }

            tilAddEditTransactionDate.setEndIconOnClickListener { fragmentViewModel.showDatePicker() }
            tilAddEditTransactionValue.setEndIconOnClickListener { fragmentViewModel.showCalculator() }
            tilAddEditTransactionDueDate.setEndIconOnClickListener { fragmentViewModel.showDueDatePicker() }

            edtAddEditTransactionContacts.apply {
                addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
            }
            fragmentViewModel.addressBook.observe(viewLifecycleOwner, { contacts ->
                edtAddEditTransactionContacts.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_layout_popup_item, contacts.values.toList()))
            })

            val bottomAppBarBackground: MaterialShapeDrawable = mBottomAppBar.background as MaterialShapeDrawable
            bottomAppBarBackground.shapeAppearanceModel = bottomAppBarBackground.shapeAppearanceModel.toBuilder().setTopEdge(
                BottomAppBarCutCornersTopEdge(mBottomAppBar.fabCradleMargin, mBottomAppBar.fabCradleRoundedCornerRadius, mBottomAppBar.cradleVerticalOffset)
            ).build()
            mBottomAppBar.setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun setupEvents() {
        fragmentViewModel.accounts.observe(viewLifecycleOwner, { accounts ->
            binding.edtAddEditTransactionAccount.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_layout_popup_item, accounts.map { it.name }))
            if (args.transaction == null && args.account == null) binding.edtAddEditTransactionAccount.setText(accounts.first().name, false)
        })
        fragmentViewModel.budgets.observe(viewLifecycleOwner, { budgets ->
            val budgetEntries = budgets.map { it.name }.toMutableList().apply { add(0, "") }
            binding.edtAddEditTransactionBudget.setAdapter(ArrayAdapter(requireContext(), R.layout.dropdown_layout_popup_item, budgetEntries))
        })

        fragmentViewModel.showDatePicker.observe(viewLifecycleOwner, EventObserver { selectedDate -> showDatePicker(selectedDate) })
        fragmentViewModel.showDueDatePicker.observe(viewLifecycleOwner, EventObserver { selectedDate -> showDueDatePicker(selectedDate) })
        fragmentViewModel.showCalculator.observe(viewLifecycleOwner, EventObserver { showCalculator() })

        fragmentViewModel.snackBarMessage.observe(viewLifecycleOwner, EventObserver {
            val text = requireContext().getString(it.stringRes, it.text)
            Snackbar.make(binding.mBottomAppBar, text, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.fabAddEditTransactionSave)
                .show()
        })

        fragmentViewModel.transactionSaved.observe(viewLifecycleOwner, { findNavController().navigateUp() })
    }

    private fun startTransition() {
        enterTransition = if (args.transitionViewResId >= 0) {
            MaterialContainerTransition(
                correctForZOrdering = true
            ).apply {
                setSharedElementViews(
                    requireActivity().findViewById(args.transitionViewResId),
                    binding.mAddEditTransactionContainer
                )
                duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                interpolator = FastOutSlowInInterpolator()
            }
        } else {
            Slide().apply {
                duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                interpolator = FastOutSlowInInterpolator()
            }
        }

        returnTransition = Slide().apply {
            duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            interpolator = AccelerateInterpolator()
        }
        startPostponedEnterTransition()
    }

    private fun showDatePicker(selectedDate: LocalDate) {
        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.ChooseDate)
            .setSelection(selectedDate.utcMillis())
            .build()

        datePickerDialog.addOnPositiveButtonClickListener {
            fragmentViewModel.transactionDate.value = it.toLocalDate().format(DateFormatUtils.SHORT_DATE_FORMAT)
            binding.edtAddEditTransactionDate.postDelayed(25) { binding.edtAddEditTransactionDate.apply { setSelection(text?.length ?: 0) } }
        }

        datePickerDialog.show(childFragmentManager, null)
    }

    private fun showDueDatePicker(selectedDate: LocalDate) {
        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.ChooseDueDate)
            .setSelection(selectedDate.utcMillis())
            .build()

        datePickerDialog.addOnPositiveButtonClickListener {
            fragmentViewModel.transactionDueDate.value = it.toLocalDate().format(DateFormatUtils.SHORT_DATE_FORMAT)
            binding.edtAddEditTransactionDueDate.postDelayed(25) { binding.edtAddEditTransactionDueDate.apply { setSelection(text?.length ?: 0) } }
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
                initialValue = binding.edtAddEditTransactionValue.getDecimalNumber().toBigDecimal()
                minValue = 0.toBigDecimal()
                isSignBtnShown = false
            }
        }.show(childFragmentManager, null)
    }

    //endregion

    //region Callback

    override fun onValueEntered(requestCode: Int, value: BigDecimal?) {
        fragmentViewModel.transactionValue.value = value?.toDouble()?.let { CurrencyTextInputEditText.CURRENCY_FORMAT.format(it) }
        binding.edtAddEditTransactionValue.postDelayed(25) { binding.edtAddEditTransactionValue.apply { setSelection(text?.length ?: 0) } }
    }

    //endregion
}