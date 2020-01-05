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

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.forEach
import androidx.core.view.postDelayed
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.transition.MaterialContainerTransition
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.ext.hasPermission
import at.guger.moneybook.core.util.ext.toEpochMilli
import at.guger.moneybook.core.util.ext.toLocalDate
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentAddEditTransactionBinding
import at.guger.moneybook.util.BottomAppBarCutCornersTopEdge
import at.guger.moneybook.util.DateFormatUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.maltaisn.calcdialog.CalcDialog
import kotlinx.android.synthetic.main.fragment_add_edit_transaction.*
import org.jetbrains.anko.find
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * Dialog fragment for creating a new [transaction][Transaction].
 */
class AddEditTransactionFragment : BaseFragment(), CalcDialog.CalcDialogCallback {

    //region Variables

    private val args: AddEditTransactionFragmentArgs by navArgs()

    private val viewModel: AddEditTransactionViewModel by viewModel()

    //endregion

    //region Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAddEditTransactionBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        check(args.transaction == null || args.account == null) { "Cannot setup with account and transaction!" }

        // TODO: Request if permission isn't granted
        if (requireContext().hasPermission(Manifest.permission.READ_CONTACTS)) viewModel.loadContacts()

        setupLayout()
        setupEvents()

        args.transaction?.let { viewModel.setupTransaction(it) }
        args.account?.let { viewModel.setupAccount(it) }

        startTransition()

        edtAddEditTransactionTitle.requestFocus()
        if (args.transaction != null) Handler().postDelayed({ edtAddEditTransactionTitle.setSelection(edtAddEditTransactionTitle.text?.length ?: 0) }, 200)
    }

    override fun onPause() {
        super.onPause()

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        TooltipCompat.setTooltipText(fabAddEditTransactionSave, getString(R.string.Save))

        edtAddEditTransactionAccount.inputType = InputType.TYPE_NULL
        edtAddEditTransactionBudget.inputType = InputType.TYPE_NULL

        mAddEditTransactionTypeToggleButtonGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            var hasChecked = false

            group.forEach { if ((it as MaterialButton).isChecked) hasChecked = true }

            if (!isChecked && !hasChecked) group.find<MaterialButton>(checkedId).isChecked = true

            if (isChecked) viewModel.onTransactionTypeChanged(checkedId)
        }

        tilAddEditTransactionDate.setEndIconOnClickListener { viewModel.showDatePicker() }
        tilAddEditTransactionValue.setEndIconOnClickListener { viewModel.showCalculator() }

        edtAddEditTransactionContacts.apply {
            addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
        }
        viewModel.addressBook.observe(viewLifecycleOwner, Observer { contacts ->
            edtAddEditTransactionContacts.setAdapter(ArrayAdapter<String>(requireContext(), R.layout.dropdown_layout_popup_item, contacts.values.toList()))
        })

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
            if (args.transaction == null && args.account == null) edtAddEditTransactionAccount.setText(accounts.first().name, false)
        })
        viewModel.budgets.observe(viewLifecycleOwner, Observer { budgets ->
            val budgetEntries = budgets.map { it.name }.toMutableList().apply { add(0, "") }
            edtAddEditTransactionBudget.setAdapter(ArrayAdapter<String>(requireContext(), R.layout.dropdown_layout_popup_item, budgetEntries))
        })

        viewModel.showDatePicker.observe(viewLifecycleOwner, EventObserver { selectedDate -> showDatePicker(selectedDate) })
        viewModel.showCalculator.observe(viewLifecycleOwner, EventObserver { showCalculator() })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(mBottomAppBar, it, Snackbar.LENGTH_LONG)
                .setAnchorView(fabAddEditTransactionSave)
                .show()
        })

        viewModel.transactionSaved.observe(viewLifecycleOwner, Observer { findNavController().navigateUp() })
    }

    private fun startTransition() {
        enterTransition = if (args.transitionViewResId >= 0) {
            MaterialContainerTransition(
                correctForZOrdering = true
            ).apply {
                setSharedElementViews(
                    requireActivity().findViewById(args.transitionViewResId),
                    mAddEditTransactionContainer
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
            .setSelection(selectedDate.atTime(LocalTime.NOON).toEpochMilli())
            .build()

        datePickerDialog.addOnPositiveButtonClickListener {
            viewModel.transactionDate.value = it.toLocalDate().format(DateFormatUtils.MEDIUM_DATE_FORMAT)
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
                initialValue = this@AddEditTransactionFragment.edtAddEditTransactionValue.getDecimalNumber().toBigDecimal()
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