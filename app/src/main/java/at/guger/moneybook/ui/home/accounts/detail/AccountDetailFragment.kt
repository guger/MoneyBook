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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.core.ui.recyclerview.layoutmanager.SnappingLinearLayoutManager
import at.guger.moneybook.core.ui.recyclerview.listener.OnItemTouchListener
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.TabListMediator
import at.guger.moneybook.core.util.ext.hideIfShown
import at.guger.moneybook.core.util.ext.resolveColor
import at.guger.moneybook.core.util.ext.setup
import at.guger.moneybook.core.util.ext.showIfHidden
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentAccountDetailBinding
import at.guger.moneybook.ui.home.addedittransaction.AddEditTransactionFragmentDirections
import at.guger.moneybook.ui.main.MainActivity
import at.guger.moneybook.util.DateFormatUtils
import at.guger.moneybook.util.menu.TransactionMenuUtils
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_account_detail.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
            tabLayout = mAccountDetailTabs,
            recyclerView = mAccountDetailRecyclerView,
            itemComparator = { i1, i2 ->
                adapter.currentList[i1].date.month == adapter.currentList[i2].date.month
            },
            itemTitleProvider = { index ->
                monthYearDateFormatter.format(adapter.currentList[index].date)
            },
            onTabChangedCallback = ::onTabSelected
        )
    }
    private val monthYearDateFormatter = DateTimeFormatter.ofPattern(DateFormatUtils.MMM_YYYY_DATE_FORMAT, Locale.getDefault())

    private val viewModel: AccountDetailViewModel by inject { parametersOf(args.accountId) }

    //endregion

    //region Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentAccountDetailBinding.inflate(inflater, container, false)

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
                    setupChart(transactions.reversed())
                }
            })
        }

        mAccountDetailRecyclerView.setup(SnappingLinearLayoutManager(requireContext()), adapter) {
            addOnItemTouchListener(OnItemTouchListener(context, this, this@AccountDetailFragment))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onTabSelected(index: Int, itemRange: IntRange) {
        val transactions = adapter.currentList

        if (transactions.size == 0) return

        val firstDateOfMonth = transactions[itemRange.last].date.withDayOfMonth(1)
        val daysOfMonth = firstDateOfMonth.lengthOfMonth().toFloat()

        val firstDate = transactions.last().date.withDayOfMonth(1)

        val xValue = firstDateOfMonth.toEpochDay() - firstDate.toEpochDay()

        mAccountDetailChart.post {
            mAccountDetailChart.setVisibleXRange(daysOfMonth, daysOfMonth)
            mAccountDetailChart.moveViewToAnimated(xValue.toFloat(), 0.0f, YAxis.AxisDependency.LEFT, 250)
        }

        if (itemRange.first == 0 || itemRange.last < transactions.size - 1) {
            fabAccountDetailAddTransaction.showIfHidden()
        } else {
            fabAccountDetailAddTransaction.hideIfShown()
        }
    }

    private fun setupChart(transactions: List<Transaction>) {
        val firstDay = transactions.firstOrNull()?.date?.withDayOfMonth(1)
        val lastDay = transactions.lastOrNull()?.date?.run { withDayOfMonth(lengthOfMonth()) }
        val days = ((lastDay?.toEpochDay() ?: 0L) - (firstDay?.toEpochDay() ?: 0)).toInt()

        val collectedDataPoints: List<Pair<LocalDate, Float>> = transactions.groupBy { it.date }.mapValues { entry ->
            entry.value.sumByDouble {
                if (it.type == Transaction.TransactionType.EARNING) it.value else -it.value
            }.toFloat()
        }.toList()

        val chartEntries = mutableListOf<Entry>()

        for (i in 0 until days) {
            val sum: Float = if (i > 0) chartEntries[i - 1].y else 0.0f

            chartEntries.add(Entry(i.toFloat(), sum + (collectedDataPoints.singleOrNull { it.first == firstDay!!.plusDays(i.toLong()) }?.second ?: 0.0f)))
        }

        val lineData = LineData(LineDataSet(chartEntries, "").apply {
            color = requireContext().resolveColor(R.attr.colorSecondaryVariant)
            lineWidth = 2.5f
            setDrawCircles(false)
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        }).apply {
            setDrawValues(false)
            isHighlightEnabled = false
        }

        with(mAccountDetailChart) {
            isScaleXEnabled = false
            isScaleYEnabled = false
            isDragEnabled = false

            xAxis.isEnabled = false

            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            description.isEnabled = false

            legend.isEnabled = false

            data = lineData

            animateX(250)
        }
    }

    private fun setupEvents() {
        viewModel.showAddEditTransactionDialogFragment.observe(viewLifecycleOwner, EventObserver { account ->
            findNavController().navigate(AddEditTransactionFragmentDirections.actionGlobalAddEditTransactionFragment(account = account, transitionViewResId = R.id.fabAccountDetailAddTransaction))
        })
    }

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