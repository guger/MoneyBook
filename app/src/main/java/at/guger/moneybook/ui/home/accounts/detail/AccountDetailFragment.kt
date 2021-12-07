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

package at.guger.moneybook.ui.home.accounts.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.chart.DateDataPoint
import at.guger.moneybook.core.util.ext.colorAttr
import at.guger.moneybook.data.model.Account
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentAccountDetailBinding
import at.guger.moneybook.ui.home.addedittransaction.AddEditTransactionFragmentDirections
import at.guger.moneybook.util.DateFormatUtils
import com.google.android.material.transition.MaterialContainerTransform
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Fragment displaying the [transactions][Transaction] of an [account][Account].
 */
class AccountDetailFragment : BaseDataBindingFragment<FragmentAccountDetailBinding, AccountDetailViewModel>() {

    //region Variables

    private val args: AccountDetailFragmentArgs by navArgs()

    private lateinit var months: List<LocalDate>

    private val monthYearDateFormatter = DateTimeFormatter.ofPattern(DateFormatUtils.MMM_YYYY_DATE_FORMAT, Locale.getDefault())

    override val fragmentViewModel by viewModel<AccountDetailViewModel> { parametersOf(args.accountId) }

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): FragmentAccountDetailBinding {
        return FragmentAccountDetailBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            scrimColor = requireContext().colorAttr(R.attr.colorPrimarySurface)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TooltipCompat.setTooltipText(binding.fabAccountDetailAddTransaction, getString(R.string.NewTransaction))

        setupLayout()
        setupEvents()
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        fragmentViewModel.transactionMonths.observe(viewLifecycleOwner) {
            months = it

            binding.mAccountDetailViewPager.adapter = object : FragmentStateAdapter(this) {
                override fun createFragment(position: Int): Fragment {
                    return AccountDetailMonthlyFragment.instantiate(args.accountId, it[position])
                }

                override fun getItemCount(): Int = it.size
            }

            binding.mAccountDetailTabs.addTabs(
                it.map { date -> monthYearDateFormatter.format(date) },
                it.indexOf(it.findLast { date ->
                    val now = LocalDate.now()

                    return@findLast date.isBefore(now) || date.isEqual(now)
                })
            )
        }

        binding.mAccountDetailTabs.setUpWithViewPager(binding.mAccountDetailViewPager)

        binding.mAccountDetailTabs.addOnPageChangeListener {
            setupChart(months[it])
        }
    }

    private fun setupChart(month: LocalDate) {
        val days = Array<LocalDate>(month.lengthOfMonth()) { i -> month.plusDays(i.toLong()) }

        fragmentViewModel.transactionsByMonth(month).observe(viewLifecycleOwner) { transactions ->
            val values = transactions.groupBy { it.date }.mapValues { map ->
                map.value.sumOf { if (it.type == Transaction.TransactionType.EARNING) it.value else -it.value }
            }

            val dataPoints = mutableListOf<DateDataPoint>()

            days.forEach { date ->
                var value = values[date]?.toFloat() ?: 0f

                if (dataPoints.isNotEmpty()) value += dataPoints.last().value

                dataPoints.add(DateDataPoint(date, value))
            }

            binding.mAccountDetailChart.setDataPoints(dataPoints)
        }
    }

    private fun setupEvents() {
        fragmentViewModel.showAddEditTransactionDialogFragment.observe(viewLifecycleOwner, EventObserver { account ->
            val extras = FragmentNavigatorExtras(binding.fabAccountDetailAddTransaction to "shared_element_container")
            findNavController().navigate(AddEditTransactionFragmentDirections.actionGlobalAddEditTransactionFragment(account = account), extras)
        })
    }

    //endregion
}