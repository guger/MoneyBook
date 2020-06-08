/*
 * Copyright 2020 Daniel Guger
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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.listener.OnItemTouchListener
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentDuesBinding
import at.guger.moneybook.ui.home.HomeViewModel
import at.guger.moneybook.ui.home.addedittransaction.AddEditTransactionFragmentDirections
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.menu.TransactionMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import kotlinx.android.synthetic.main.fragment_dues.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * Fragment for [home view pager's][ViewPager2] bills content.
 */
class DuesFragment : BaseFragment(), OnItemTouchListener.ItemTouchListener {

    //region Variables

    lateinit var adapter: DuesAdapter

    private val viewModel: HomeViewModel by sharedViewModel()

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentDuesBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DuesAdapter().apply { viewModel.claimsAndDebts.observe(viewLifecycleOwner, Observer(::submitList)) }

        mDuesRecyclerView.setup(LinearLayoutManager(context), adapter, hasFixedSize = false) {
            addOnItemTouchListener(OnItemTouchListener(context, this, this@DuesFragment))
        }
    }

    //endregion

    //region Methods

    private fun editTransaction(transaction: Transaction) {
        findNavController().navigate(AddEditTransactionFragmentDirections.actionGlobalAddEditTransactionFragment(transaction))
    }

    //endregion

    //region Callback

    override fun onItemClick(view: View, pos: Int, e: MotionEvent) {
        if (requireAppCompatActivity<MainActivity>().mCab.isActive()) {
            adapter.toggleChecked(pos)

            if (adapter.checkedCount > 0) {
                requireAppCompatActivity<MainActivity>().mCab!!.apply {
                    title(literal = getString(R.string.x_selected, adapter.checkedCount))

                    TransactionMenuUtils.prepareMenu(getMenu(), adapter, markAsPaid = true)
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

                    onCreate { _, menu -> TransactionMenuUtils.prepareMenu(menu, adapter, markAsPaid = true) }

                    onDestroy {
                        adapter.clearChecked()
                        true
                    }

                    onSelection { menuItem ->
                        TransactionMenuUtils.onItemSelected(menuItem, adapter, ::editTransaction, viewModel::markAsPaid, viewModel::deleteTransaction)
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

    companion object {
        fun instantiate(): DuesFragment = DuesFragment()
    }
}