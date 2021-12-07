/*
 * Copyright 2021 Daniel Guger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.guger.moneybook.ui.home.accounts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseViewBindingFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.LayoutRecyclerViewBinding
import at.guger.moneybook.ui.home.addedittransaction.AddEditTransactionFragmentDirections
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.menu.TransactionMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

/**
 * Fragment displaying the monthly [transactions][Transaction] of an [account][Account].
 */
class AccountDetailMonthlyFragment : BaseViewBindingFragment<LayoutRecyclerViewBinding>() {

    //region Variables

    private lateinit var adapter: AccountDetailTransactionsListAdapter

    private val month: LocalDate by lazy { requireArguments()[KEY_MONTH] as LocalDate }

    private val fragmentViewModel by viewModel<AccountDetailViewModel> { parametersOf(requireArguments()[KEY_ACCOUNT_ID]) }

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): LayoutRecyclerViewBinding {
        return LayoutRecyclerViewBinding.inflate(inflater, parent, attachToParent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
    }

    override fun onResume() {
        super.onResume()

        fragmentViewModel.onItemClick.observe(viewLifecycleOwner, EventObserver(::onItemClick))
        fragmentViewModel.onItemLongClick.observe(viewLifecycleOwner, EventObserver(::onItemLongClick))
    }

    override fun onPause() {
        super.onPause()

        fragmentViewModel.onItemClick.removeObservers(viewLifecycleOwner)
        fragmentViewModel.onItemLongClick.removeObservers(viewLifecycleOwner)
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        adapter = AccountDetailTransactionsListAdapter(fragmentViewModel)

        binding.mLayoutRecyclerView.setup(LinearLayoutManager(requireContext()), adapter)

        fragmentViewModel.transactionsByMonth(month).observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun editTransaction(pos: Int, transaction: Transaction) {
        findNavController().navigate(AddEditTransactionFragmentDirections.actionGlobalAddEditTransactionFragment(transaction))
    }

    //endregion

    //region Callback

    private fun onItemClick(pos: Int) {
        if (getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter)
                }
            } else {
                getAppCompatActivity<MainActivity>()?.destroyCab()
            }
        }
    }

    private fun onItemLongClick(pos: Int) {
        adapter.toggleChecked(pos)

        if (adapter.checkedCount > 0) {
            if (!getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
                getAppCompatActivity<MainActivity>()?.attachCab(R.menu.menu_transaction) {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    onCreate { _, menu -> TransactionMenuUtils.prepareMenu(menu, adapter) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        TransactionMenuUtils.onItemSelected(menuItem, adapter, editAction = ::editTransaction, deleteAction = fragmentViewModel::delete)
                        destroy()
                    }
                }
            } else {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter)
                }
            }
        } else {
            getAppCompatActivity<MainActivity>()?.destroyCab()
        }
    }

    //endregion

    companion object {
        private const val KEY_ACCOUNT_ID: String = "key_account_id"
        private const val KEY_MONTH: String = "key_month"

        fun instantiate(accountId: Long, month: LocalDate): AccountDetailMonthlyFragment {
            return AccountDetailMonthlyFragment().apply {
                arguments = bundleOf(KEY_ACCOUNT_ID to accountId, KEY_MONTH to month)
            }
        }
    }
}