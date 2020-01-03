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

package at.guger.moneybook.ui.home.budgets.addeditbudget

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.databinding.DialogFragmentAddEditBudgetBinding
import at.guger.moneybook.util.DataUtils
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_add_edit_budget.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Dialog fragment for creating a new [accounts][Account].
 */
class AddEditBudgetBottomSheetDialogFragment : BottomSheetDialogFragment() {

    //region Variables

    private val args: AddEditBudgetBottomSheetDialogFragmentArgs by navArgs()

    private val viewModel: AddEditBudgetDialogFragmentViewModel by viewModel()

    //endregion

    //region DialogFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogFragmentAddEditBudgetBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.budget?.let { viewModel.setupBudget(it) }

        setupEvents()

        edtAddEditBudgetName.requestFocus()

        if (args.budget != null) Handler().postDelayed({ edtAddEditBudgetName.setSelection(edtAddEditBudgetName.text?.length ?: 0) }, 200)
    }

    //endregion

    //region Methods

    private fun setupEvents() {
        viewModel.budgetName.observe(viewLifecycleOwner, Observer { viewModel.onTextFieldChanged() })
        viewModel.budgetBudget.observe(viewLifecycleOwner, Observer { viewModel.onTextFieldChanged() })
        viewModel.isValidForm.observe(viewLifecycleOwner, Observer { btnAddEditBudgetSave.isEnabled = it })

        viewModel.showColorChooser.observe(viewLifecycleOwner, EventObserver {
            MaterialDialog(requireActivity()).show {
                title(res = R.string.ChooseColor)

                colorChooser(colors = DataUtils.getBudgetColors(requireContext()), initialSelection = viewModel.budgetColor.value) { _, color ->
                    viewModel.budgetColor.value = color
                }

                negativeButton(res = R.string.Cancel)
                positiveButton(res = R.string.ChooseColor)
            }
        })

        viewModel.budgetSaved.observe(viewLifecycleOwner, EventObserver { findNavController().navigateUp() })
    }

    //endregion
}