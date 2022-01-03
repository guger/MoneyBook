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

package at.guger.moneybook.ui.home.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.databinding.FragmentAccountsBinding
import at.guger.moneybook.ui.home.HomeFragmentDirections
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.menu.AccountMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] accounts content.
 */
class AccountsFragment : BaseDataBindingFragment<FragmentAccountsBinding, HomeViewModel>() {

    //region Variables

    private lateinit var adapter: AccountsAdapter

    override val fragmentViewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): FragmentAccountsBinding {
        return FragmentAccountsBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AccountsAdapter(fragmentViewModel).apply {
            fragmentViewModel.accounts.observe(viewLifecycleOwner, Observer(::submitList))
        }

        binding.mAccountsRecyclerView.setup(LinearLayoutManager(requireContext()), adapter, hasFixedSize = false)
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

    private fun editAccount(account: Account) {
        findNavController().navigate(MainNavDirections.actionGlobalAddEditAccountBottomSheetDialogFragment(account))
    }

    private fun deleteAccount(vararg account: Account) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(if (account.size == 1) R.string.DeleteAccount else R.string.DeleteAccounts)
            .setMessage(getString(if (account.size == 1) R.string.aldm_DeleteAccount else R.string.aldm_DeleteAccounts, account.joinToString { it.name }))
            .setPositiveButton(R.string.Delete) { _, _ -> fragmentViewModel.deleteAccount(*account) }
            .setNegativeButton(R.string.Cancel, null)
            .show()
    }

    //endregion

    //region Callback

    private fun onItemClick(pos: Int) {
        if (getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    AccountMenuUtils.prepareMenu(getMenu(), adapter)
                }
            } else {
                getAppCompatActivity<MainActivity>()?.destroyCab()
            }
        } else {
            val itemView = binding.mAccountsRecyclerView.getChildAt(pos)
            val accountDetailTransitionName = getString(R.string.fragment_detail_transition_name)
            val extras = FragmentNavigatorExtras(itemView to accountDetailTransitionName)
            val directions = HomeFragmentDirections.actionHomeFragmentToAccountDetailFragment(adapter.currentList[pos].id)
            findNavController().navigate(directions, extras)
        }
    }

    private fun onItemLongClick(pos: Int) {
        adapter.toggleChecked(pos)

        if (adapter.checkedCount > 0) {
            if (!getAppCompatActivity<MainActivity>()?.mCab.isActive()) {
                getAppCompatActivity<MainActivity>()?.attachCab(R.menu.menu_account) {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    onCreate { _, menu -> AccountMenuUtils.prepareMenu(menu, adapter) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        AccountMenuUtils.onItemSelected(menuItem, adapter, ::editAccount, ::deleteAccount)
                        destroy()
                    }
                }
            } else {
                getAppCompatActivity<MainActivity>()?.mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    AccountMenuUtils.prepareMenu(getMenu(), adapter)
                }
            }
        } else {
            getAppCompatActivity<MainActivity>()?.destroyCab()
        }
    }

    //endregion

    companion object {
        fun instantiate(): AccountsFragment = AccountsFragment()
    }
}