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

package at.guger.moneybook.ui.home.dues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentDuesBinding
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.home.addedittransaction.AddEditTransactionFragmentDirections
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.menu.TransactionMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] dues content.
 */
class DuesFragment : BaseDataBindingFragment<FragmentDuesBinding, HomeViewModel>() {

    //region Variables

    private lateinit var adapter: DuesAdapter

    override val fragmentViewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): FragmentDuesBinding {
        return FragmentDuesBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DuesAdapter(fragmentViewModel).apply { fragmentViewModel.claimsAndDebts.observe(viewLifecycleOwner, Observer(::submitList)) }

        binding.mDuesRecyclerView.setup(LinearLayoutManager(context), adapter, hasFixedSize = false)

        setupEvents()
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

    private fun setupEvents() {
        fragmentViewModel.moveToAccount.observe(viewLifecycleOwner, EventObserver { (transactions, accounts) ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ChooseAccount)
                .setItems(accounts.map { it.name }.toTypedArray()) { _, which ->
                    fragmentViewModel.moveToAccount(accounts[which], *transactions)
                }
                .show()
        })
    }

    private fun editTransaction(pos: Int, transaction: Transaction) {
        findNavController().navigate(AddEditTransactionFragmentDirections.actionGlobalAddEditTransactionFragment(transaction))
    }

    private fun markAsPaid(vararg transaction: Transaction, moveToAccount: Boolean) {
        fragmentViewModel.markAsPaid(*transaction, moveToAccount = moveToAccount)
    }

    //endregion

    //region Callback

    private fun onItemClick(pos: Int) {
        if (getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter, markAsPaid = true)
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

                    onCreate { _, menu -> TransactionMenuUtils.prepareMenu(menu, adapter, markAsPaid = true) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        TransactionMenuUtils.onItemSelected(menuItem, adapter, ::editTransaction, ::markAsPaid, fragmentViewModel::deleteTransaction)
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
        fun instantiate(): DuesFragment = DuesFragment()
    }
}