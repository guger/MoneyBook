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

package at.guger.moneybook.ui.home.addedittransaction

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.postDelayed
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.permission.Permission
import at.guger.moneybook.core.permission.PermissionManager
import at.guger.moneybook.core.permission.RationaleInfo
import at.guger.moneybook.core.ui.fragment.BaseDataBindingFragment
import at.guger.moneybook.core.ui.shape.BottomAppBarCutCornersTopEdge
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.CurrencyTextInputEditText
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.core.util.ext.toLocalDate
import at.guger.moneybook.core.util.ext.utcMillis
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.AccountWithBalance
import at.guger.moneybook.data.model.BudgetWithBalance
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentAddEditTransactionBinding
import at.guger.moneybook.util.CurrencyFormat
import at.guger.moneybook.util.DateFormatUtils
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_DROPDOWN_MENU
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.maltaisn.calcdialog.CalcDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate

/**
 * Dialog fragment for adding/editing a [transaction][Transaction].
 */
class AddEditTransactionFragment : BaseDataBindingFragment<FragmentAddEditTransactionBinding, AddEditTransactionViewModel>(),
    CalcDialog.CalcDialogCallback {

    //region Variables

    private val args by navArgs<AddEditTransactionFragmentArgs>()

    override val fragmentViewModel: AddEditTransactionViewModel by viewModel()

    private lateinit var accounts: List<AccountWithBalance>
    private lateinit var budgets: List<BudgetWithBalance>

    private val permissionManager = PermissionManager.from(this)

    //endregion

    //region Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            setPathMotion(MaterialArcMotion())
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        root: ViewGroup?,
        attachToParent: Boolean
    ): FragmentAddEditTransactionBinding {
        return FragmentAddEditTransactionBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        check(args.transaction == null || args.account == null) { "Cannot setup with account and transaction!" }

        permissionManager.checkPermission(Permission.CONTACTS, callback = { isAllGranted, _ ->
            if (isAllGranted) {
                fragmentViewModel.loadContacts()
            } else {
                binding.tilAddEditTransactionContacts.apply {
                    setEndIconDrawable(R.drawable.ic_contacts_permission)
                    endIconMode = END_ICON_CUSTOM

                    setEndIconOnClickListener {
                        requestPermission()
                    }
                }
            }
        })

        setupLayout()
        setupEvents()

        args.transaction?.let { fragmentViewModel.setupTransaction(it) }
        args.account?.let { fragmentViewModel.setupAccount(it) }

        binding.edtAddEditTransactionTitle.requestFocus()
        if (args.transaction != null) Handler(Looper.getMainLooper()).postDelayed({
            binding.edtAddEditTransactionTitle.setSelection(binding.edtAddEditTransactionTitle.text?.length ?: 0)
        }, 200)
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

            edtAddEditTransactionAccount.setOnItemClickListener { _, _, i, _ ->
                val account = accounts[i]
                tilAddEditTransactionAccount.helperText = getString(R.string.x_available, CurrencyFormat.format(account.balance))
            }

            edtAddEditTransactionBudget.setOnItemClickListener { _, _, i, _ ->
                if (i > 0) {
                    val budget = budgets[i - 1]
                    tilAddEditTransactionBudget.isHelperTextEnabled = true
                    tilAddEditTransactionBudget.helperText = getString(R.string.x_left, CurrencyFormat.format(budget.left))
                } else {
                    tilAddEditTransactionBudget.isHelperTextEnabled = false
                }
            }

            mAddEditTransactionTypeToggleButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) fragmentViewModel.onTransactionTypeChanged(checkedId)
            }

            tilAddEditTransactionDate.setEndIconOnClickListener { fragmentViewModel.showDatePicker() }
            tilAddEditTransactionValue.setEndIconOnClickListener { fragmentViewModel.showCalculator() }
            tilAddEditTransactionDueDate.setEndIconOnClickListener { fragmentViewModel.showDueDatePicker() }

            setupSuggestions(this)

            edtAddEditTransactionContacts.apply {
                addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
            }
            fragmentViewModel.addressBook.observe(viewLifecycleOwner) { contacts ->
                edtAddEditTransactionContacts.setAdapter(
                    ArrayAdapter(
                        requireContext(),
                        R.layout.dropdown_layout_popup_item,
                        contacts.values.toList()
                    )
                )
                tilAddEditTransactionContacts.endIconMode = END_ICON_DROPDOWN_MENU
            }

            val bottomAppBarBackground: MaterialShapeDrawable = mBottomAppBar.background as MaterialShapeDrawable
            bottomAppBarBackground.shapeAppearanceModel = bottomAppBarBackground.shapeAppearanceModel.toBuilder().setTopEdge(
                BottomAppBarCutCornersTopEdge(
                    mBottomAppBar.fabCradleMargin,
                    mBottomAppBar.fabCradleRoundedCornerRadius,
                    mBottomAppBar.cradleVerticalOffset
                )
            ).build()

            mBottomAppBar.apply {
                setOnMenuItemClickListener { item: MenuItem ->
                    return@setOnMenuItemClickListener when (item.itemId) {
                        R.id.actionTransferToAccount -> {
                            if (fragmentViewModel.checkTransactionForm()) {
                                fragmentViewModel.transferAccounts.observe(viewLifecycleOwner) { (accounts, selectedAccount) ->
                                    val targetAccounts: List<Account> = accounts.toMutableList().apply { remove(selectedAccount) }

                                    MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(R.string.ChooseAccount)
                                        .setItems(targetAccounts.map { it.name }.toTypedArray()) { _, which ->
                                            fragmentViewModel.transferTransaction(
                                                requireContext(),
                                                edtAddEditTransactionContacts.chipValues,
                                                targetAccounts[which]
                                            )
                                        }
                                        .show()
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }

                setNavigationOnClickListener { findNavController().navigateUp() }
            }
        }
    }

    private fun setupSuggestions(binding: FragmentAddEditTransactionBinding) {
        with(binding) {
            if (args.transaction == null) {
                var job: Job? = null

                edtAddEditTransactionTitle.doOnTextChanged { text, _, _, _ ->
                    if (text != null && text.isNotBlank()) {
                        job?.cancel()

                        job = lifecycle.coroutineScope.launch {
                            fragmentViewModel.findTransactions(text.trim().toString()).collect { items ->
                                binding.chgAddEditTransactionSuggestions.removeAllViews()

                                items.forEach {
                                    val chip = Chip(requireContext()).apply {
                                        setText(getString(R.string.SuggestionText, it.title, CurrencyFormat.format(it.value)))
                                        setOnClickListener { _ ->
                                            fragmentViewModel.setupSuggestion(it)
                                        }
                                        setOnLongClickListener { _ ->
                                            fragmentViewModel.setupSuggestionWithoutValue(it)
                                            return@setOnLongClickListener true
                                        }
                                    }

                                    binding.mAddEditTransactionHorizontalScrollView.visibility = View.VISIBLE
                                    binding.chgAddEditTransactionSuggestions.addView(chip)
                                }
                            }
                        }
                    } else {
                        job?.cancel()
                        binding.mAddEditTransactionHorizontalScrollView.visibility = View.GONE
                        binding.chgAddEditTransactionSuggestions.removeAllViews()
                    }
                }
            }
        }
    }

    private fun setupEvents() {
        fragmentViewModel.accounts.observe(viewLifecycleOwner) { accounts ->
            this.accounts = accounts
            binding.edtAddEditTransactionAccount.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_layout_popup_item,
                    accounts.map { it.name })
            )

            if (args.transaction == null && args.account == null) {
                binding.edtAddEditTransactionAccount.setText(accounts.first().name, false)
            }

            val balance: Double = when {
                args.transaction?.type == Transaction.TransactionType.EARNING || args.transaction?.type == Transaction.TransactionType.EXPENSE -> {
                    accounts.find { it.id == args.transaction!!.account!!.id }!!.balance
                }
                args.account != null -> {
                    accounts.find { it.id == args.account!!.id }!!.balance
                }
                else -> {
                    accounts[0].balance
                }
            }
            binding.tilAddEditTransactionAccount.helperText = getString(R.string.x_available, CurrencyFormat.format(balance))
        }
        fragmentViewModel.budgets.observe(viewLifecycleOwner) { budgets ->
            this.budgets = budgets
            val budgetEntries = budgets.map { it.name }.toMutableList().apply { add(0, "") }
            binding.edtAddEditTransactionBudget.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_layout_popup_item,
                    budgetEntries
                )
            )

            args.transaction?.budget?.let { transactionBudget ->
                val budget = budgets.find { it.id == transactionBudget.id }!!
                binding.tilAddEditTransactionBudget.isHelperTextEnabled = true
                binding.tilAddEditTransactionBudget.helperText = getString(R.string.x_left, CurrencyFormat.format(budget.left))
            }
        }

        fragmentViewModel.transferTransactionVisibility.observe(viewLifecycleOwner) { state ->
            binding.mBottomAppBar.menu.findItem(R.id.actionTransferToAccount).isVisible = state
        }

        fragmentViewModel.showDatePicker.observe(viewLifecycleOwner, EventObserver { selectedDate -> showDatePicker(selectedDate) })
        fragmentViewModel.showDueDatePicker.observe(
            viewLifecycleOwner,
            EventObserver { selectedDate -> showDueDatePicker(selectedDate) })
        fragmentViewModel.showCalculator.observe(viewLifecycleOwner, EventObserver { showCalculator() })

        fragmentViewModel.showOverlayPermissionDialog.observe(viewLifecycleOwner, EventObserver { chippedContacts ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.OverlayRequired)
                .setMessage(R.string.OverlayRequiredMessage)
                .setPositiveButton(R.string.OpenSettings) { _, _ ->
                    if (Utils.isMarshmallow()) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
                .setNegativeButton(R.string.Ignore) { _, _ ->
                    fragmentViewModel.saveTransaction(
                        requireContext(),
                        chippedContacts,
                        ignoreOverlayPermission = true
                    )
                }
                .show()
        })

        fragmentViewModel.snackBarMessage.observe(viewLifecycleOwner, EventObserver {
            Snackbar.make(binding.mBottomAppBar, getString(it.stringRes, it.text), Snackbar.LENGTH_LONG).apply {
                anchorView = binding.fabAddEditTransactionSave
            }.show()
        })

        fragmentViewModel.snoozeDisabledMessage.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(it.stringRes, it.text), Toast.LENGTH_LONG).show()
        })

        fragmentViewModel.transactionSaved.observe(viewLifecycleOwner) { findNavController().navigateUp() }
    }

    private fun requestPermission() {
        permissionManager.requestPermission(Permission.CONTACTS,
            info = RationaleInfo(
                titleRes = R.string.ContactsPermission,
                messageRes = R.string.ContactsPermissionNeeded
            ),
            callback = { isAllGranted, permissions ->
                if (isAllGranted) {
                    fragmentViewModel.loadContacts()
                } else if (permissions.any { !it.value && !it.key.requiresRationale(this@AddEditTransactionFragment) }) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.ContactsPermissionPermanentlyDenied)
                        .setMessage(R.string.ContactsPermissionPermanentlyDeniedMessage)
                        .setPositiveButton(R.string.OpenSettings) { _, _ ->
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", requireActivity().packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                        .setNegativeButton(R.string.Close, null)
                        .show()
                }
            }
        )
    }

    private fun showDatePicker(selectedDate: LocalDate) {
        val datePickerDialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.ChooseDate)
            .setSelection(selectedDate.utcMillis())
            .build()

        datePickerDialog.addOnPositiveButtonClickListener {
            fragmentViewModel.transactionDate.value = it.toLocalDate().format(DateFormatUtils.SHORT_DATE_FORMAT)
            binding.edtAddEditTransactionDate.postDelayed(25) {
                binding.edtAddEditTransactionDate.apply {
                    setSelection(
                        text?.length ?: 0
                    )
                }
            }
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
            binding.edtAddEditTransactionDueDate.postDelayed(25) {
                binding.edtAddEditTransactionDueDate.apply {
                    setSelection(
                        text?.length ?: 0
                    )
                }
            }
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
        binding.edtAddEditTransactionValue.postDelayed(25) {
            binding.edtAddEditTransactionValue.apply {
                setSelection(
                    text?.length ?: 0
                )
            }
        }
    }

    //endregion
}