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

package at.guger.moneybook.ui.home.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.decoration.SpacesItemDecoration
import at.guger.moneybook.core.ui.recyclerview.listener.OnItemTouchListener
import at.guger.moneybook.core.util.ext.dimen
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.databinding.FragmentAccountsBinding
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.menu.AccountMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_accounts.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] accounts content.
 */
class AccountsFragment : BaseFragment(), OnItemTouchListener.ItemTouchListener {

    //region Variables

    private lateinit var adapter: AccountsAdapter
    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentAccountsBinding>(inflater, R.layout.fragment_accounts, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AccountsAdapter(viewModel).apply { viewModel.coloredAccounts.observe(viewLifecycleOwner, Observer(::submitList)) }

        mAccountsRecyclerView.setup(LinearLayoutManager(requireContext()), adapter) {
            addOnItemTouchListener(OnItemTouchListener(requireContext(), this, this@AccountsFragment))
            addItemDecoration(SpacesItemDecoration(all = context.dimen(res = R.dimen.recyclerview_item_spacing).toInt()))
        }
    }

    //endregion

    //region Methods

    private fun editAccount(account: Account) {
        findNavController().navigate(MainNavDirections.actionGlobalAddEditAccountBottomSheetDialogFragment(account))
    }

    private fun deleteAccount(vararg account: Account) {
        MaterialDialog(requireContext()).show {
            title(res = if (account.size == 1) R.string.DeleteAccount else R.string.DeleteAccounts)
            message(text = getString(if (account.size == 1) R.string.aldm_DeleteAccount else R.string.aldm_DeleteAccounts, account.joinToString { it.name }))

            positiveButton(res = R.string.Delete) { viewModel.deleteAccount(*account) }
            negativeButton(res = R.string.Cancel)
        }
    }

    //endregion

    //region Callback

    override fun onItemClick(view: View, pos: Int, e: MotionEvent) {
        if (requireAppCompatActivity<MainActivity>().mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    AccountMenuUtils.prepareMenu(getMenu(), adapter)
                }
            } else {
                requireAppCompatActivity<MainActivity>().destroyCab()
            }
        } else {
            viewModel.showAccount(adapter.currentList[pos])
        }
    }

    override fun onItemLongClick(view: View, pos: Int, e: MotionEvent) {
        adapter.toggleChecked(pos)

        if (adapter.checkedCount > 0) {
            if (!requireAppCompatActivity<MainActivity>().mCab.isActive()) {
                requireAppCompatActivity<MainActivity>().attachCab(R.menu.menu_account) {
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
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    AccountMenuUtils.prepareMenu(getMenu(), adapter)
                }
            }
        } else {
            requireAppCompatActivity<MainActivity>().destroyCab()
        }
    }

    //endregion

    companion object {
        fun instantiate(): AccountsFragment = AccountsFragment()
    }
}