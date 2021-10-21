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

package at.guger.moneybook.ui.home.budgets.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseViewBindingFragment
import at.guger.moneybook.core.ui.recyclerview.listener.OnItemTouchListener
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
class BudgetDetailMonthlyFragment : BaseViewBindingFragment<LayoutRecyclerViewBinding>(), OnItemTouchListener.ItemTouchListener {

    //region Variables

    private val adapter: BudgetDetailTransactionsListAdapter = BudgetDetailTransactionsListAdapter()

    private val month: LocalDate by lazy { requireArguments()[KEY_MONTH] as LocalDate }

    private val viewModel by viewModel<BudgetDetailViewModel> { parametersOf(requireArguments()[KEY_BUDGET_ID]) }

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

        binding.mLayoutRecyclerView.requestFocus()
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        binding.mLayoutRecyclerView.setup(LinearLayoutManager(requireContext()), adapter) {
            addOnItemTouchListener(OnItemTouchListener(requireContext(), this, this@BudgetDetailMonthlyFragment))
        }

        viewModel.transactionsByMonth(month).observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun editTransaction(transaction: Transaction) {
        requireParentFragment().findNavController().navigate(AddEditTransactionFragmentDirections.actionGlobalAddEditTransactionFragment(transaction))
    }

    //endregion

    //region Callback

    override fun onItemClick(view: View, pos: Int, e: MotionEvent) {
        if (getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter, isBudgets = true)
                }
            } else {
                getAppCompatActivity<MainActivity>()?.destroyCab()
            }
        }
    }

    override fun onItemLongClick(view: View, pos: Int, e: MotionEvent) {
        adapter.toggleChecked(pos)

        if (adapter.checkedCount > 0) {
            if (!getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
                getAppCompatActivity<MainActivity>()?.attachCab(R.menu.menu_transaction) {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    onCreate { _, menu -> TransactionMenuUtils.prepareMenu(menu, adapter, isBudgets = true) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        TransactionMenuUtils.onItemSelected(menuItem, adapter, editAction = ::editTransaction, deleteAction = viewModel::delete)
                        destroy()
                    }
                }
            } else {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter, isBudgets = true)
                }
            }
        } else {
            getAppCompatActivity<MainActivity>()?.destroyCab()
        }
    }

    //endregion

    companion object {
        private const val KEY_BUDGET_ID: String = "key_budget_id"
        private const val KEY_MONTH: String = "key_month"

        fun instantiate(budgetId: Long, month: LocalDate): BudgetDetailMonthlyFragment {
            return BudgetDetailMonthlyFragment().apply {
                arguments = bundleOf(KEY_BUDGET_ID to budgetId, KEY_MONTH to month)
            }
        }
    }
}