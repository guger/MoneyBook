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

package at.guger.moneybook.ui.home.accounts.addeditaccount

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingBottomSheetDialogFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.databinding.DialogFragmentAddEditAccountBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Dialog fragment for creating a new [accounts][Account].
 */
class AddEditAccountBottomSheetDialogFragment : BaseDataBindingBottomSheetDialogFragment<DialogFragmentAddEditAccountBinding, AddEditAccountDialogFragmentViewModel>() {

    //region Variables

    private val args: AddEditAccountBottomSheetDialogFragmentArgs by navArgs()

    override val fragmentViewModel: AddEditAccountDialogFragmentViewModel by viewModel()

    //endregion

    //region DialogFragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): DialogFragmentAddEditAccountBinding {
        return DialogFragmentAddEditAccountBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.account?.let { fragmentViewModel.setupAccount(it) }

        setupEvents()

        binding.edtAddEditAccountName.requestFocus()

        if (args.account != null) Handler(Looper.getMainLooper()).postDelayed({ binding.edtAddEditAccountName.setSelection(binding.edtAddEditAccountName.text?.length ?: 0) }, 200)
    }

    //endregion

    //region Methods

    private fun setupEvents() {
        fragmentViewModel.accountName.observe(viewLifecycleOwner, {
            binding.btnAddEditAccountSave.isEnabled = it.isNotBlank()
        })

        fragmentViewModel.accountStartBalanceError.observe(viewLifecycleOwner, EventObserver {
            binding.tilAddEditAccountStartBalance.error = getString(R.string.InvalidValue)
        })

        binding.edtAddEditAccountStartBalance.doOnTextChanged { _, _, _, _ -> binding.tilAddEditAccountStartBalance.error = null }

        fragmentViewModel.accountSaved.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigateUp()
        })
    }

    //endregion
}