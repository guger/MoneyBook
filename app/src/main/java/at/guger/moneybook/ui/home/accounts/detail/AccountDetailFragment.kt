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

package at.guger.moneybook.ui.home.accounts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.layoutmanager.SnappingLinearLayoutManager
import at.guger.moneybook.core.ui.recyclerview.listener.OnItemTouchListener
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.TabListMediator
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentAccountDetailBinding
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.DateFormatUtils
import at.guger.moneybook.util.menu.TransactionMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import kotlinx.android.synthetic.main.fragment_account_detail.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Fragment displaying the [transactions][Transaction] of an [account][Account].
 */
class AccountDetailFragment : BaseFragment(), OnItemTouchListener.ItemTouchListener {

    //region Variables

    private val args: AccountDetailFragmentArgs by navArgs()

    private lateinit var adapter: AccountDetailTransactionsListAdapter

    private val tabListMediator: TabListMediator by lazy {
        TabListMediator(
            mAccountDetailTabs,
            mAccountDetailRecyclerView,
            { i1, i2 ->
                adapter.currentList[i1].date.month == adapter.currentList[i2].date.month
            },
            { index ->
                monthYearDateFormatter.format(adapter.currentList[index].date)
            }
        )
    }
    private val monthYearDateFormatter = DateTimeFormatter.ofPattern(DateFormatUtils.MMM_YYYY_DATE_FORMAT, Locale.getDefault())

    private val viewModel: AccountDetailViewModel by inject { parametersOf(args.accountId) }

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentAccountDetailBinding>(inflater, R.layout.fragment_account_detail, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TooltipCompat.setTooltipText(fabAccountDetailAddTransaction, getString(R.string.NewTransaction))

        setupLayout()
        setupEvents()
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        adapter = AccountDetailTransactionsListAdapter().apply {
            viewModel.transactions.observe(viewLifecycleOwner, Observer { transactions ->
                adapter.submitList(transactions) {
                    tabListMediator.attach()
                }
            })
        }

        mAccountDetailRecyclerView.setup(SnappingLinearLayoutManager(requireContext()), adapter) {
            addOnItemTouchListener(OnItemTouchListener(context, this, this@AccountDetailFragment))
        }
    }

    private fun setupEvents() {
        viewModel.showAddEditTransactionDialogFragment.observe(viewLifecycleOwner, EventObserver { account ->
            findNavController().navigate(MainNavDirections.actionGlobalAddEditTransactionDialogFragment(account = account))
        })
    }

    private fun editTransaction(transaction: Transaction) {
        findNavController().navigate(MainNavDirections.actionGlobalAddEditTransactionDialogFragment(transaction))
    }

    //endregion

    //region Callback

    override fun onItemClick(view: View, pos: Int, e: MotionEvent) {
        if (requireAppCompatActivity<MainActivity>().mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter)
                }
            } else {
                requireAppCompatActivity<MainActivity>().destroyCab()
            }
        }
    }

    override fun onItemLongClick(view: View, pos: Int, e: MotionEvent) {
        adapter.toggleChecked(pos)

        if (adapter.checkedCount > 0) {
            if (!requireAppCompatActivity<MainActivity>().mCab.isActive()) {
                requireAppCompatActivity<MainActivity>().attachCab(R.menu.menu_transaction) {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    onCreate { _, menu -> TransactionMenuUtils.prepareMenu(menu, adapter) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        TransactionMenuUtils.onItemSelected(menuItem, adapter, ::editTransaction, viewModel::delete)
                        destroy()
                    }
                }
            } else {
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter)
                }
            }
        } else {
            requireAppCompatActivity<MainActivity>().destroyCab()
        }
    }

    //endregion
}