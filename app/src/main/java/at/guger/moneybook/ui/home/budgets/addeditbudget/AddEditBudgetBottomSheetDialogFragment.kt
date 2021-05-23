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

package at.guger.moneybook.ui.home.budgets.addeditbudget

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingBottomSheetDialogFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.databinding.DialogFragmentAddEditBudgetBinding
import at.guger.moneybook.util.DataUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Dialog fragment for creating a new [budget][Budget].
 */
class AddEditBudgetBottomSheetDialogFragment : BaseDataBindingBottomSheetDialogFragment<DialogFragmentAddEditBudgetBinding, AddEditBudgetDialogFragmentViewModel>() {

    //region Variables

    private val args: AddEditBudgetBottomSheetDialogFragmentArgs by navArgs()

    override val fragmentViewModel: AddEditBudgetDialogFragmentViewModel by viewModel()

    //endregion

    //region DialogFragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): DialogFragmentAddEditBudgetBinding {
        return DialogFragmentAddEditBudgetBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.budget?.let { fragmentViewModel.setupBudget(it) }

        setupEvents()

        binding.edtAddEditBudgetName.requestFocus()

        if (args.budget != null) Handler(Looper.getMainLooper()).postDelayed({
            binding.edtAddEditBudgetName.setSelection(binding.edtAddEditBudgetName.text?.length ?: 0)
        }, 200)
    }

    //endregion

    //region Methods

    private fun setupEvents() {
        fragmentViewModel.budgetName.observe(viewLifecycleOwner, { fragmentViewModel.onTextFieldChanged() })
        fragmentViewModel.budgetBudget.observe(viewLifecycleOwner, { fragmentViewModel.onTextFieldChanged() })
        fragmentViewModel.isValidForm.observe(viewLifecycleOwner, { binding.btnAddEditBudgetSave.isEnabled = it })

        fragmentViewModel.showColorChooser.observe(viewLifecycleOwner, EventObserver {
            MaterialDialog(requireActivity()).show {
                title(res = R.string.ChooseColor)

                colorChooser(colors = DataUtils.getBudgetColors(requireContext()), initialSelection = fragmentViewModel.budgetColor.value) { _, color ->
                    fragmentViewModel.budgetColor.value = color
                }

                negativeButton(res = R.string.Cancel)
                positiveButton(res = R.string.ChooseColor)
            }
        })

        fragmentViewModel.budgetSaved.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigateUp()
        })
    }

    //endregion
}