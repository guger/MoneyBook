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

package at.guger.moneybook.ui.home.budgets.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingFragment
import at.guger.moneybook.core.ui.widget.chart.DateDataPoint
import at.guger.moneybook.core.util.ext.colorAttr
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentBudgetDetailBinding
import at.guger.moneybook.util.DateFormatUtils
import com.google.android.material.transition.MaterialContainerTransform
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Fragment displaying the [transactions][Transaction] of a [budget][Budget].
 */
class BudgetDetailFragment : BaseDataBindingFragment<FragmentBudgetDetailBinding, BudgetDetailViewModel>() {

    //region Variables

    private val args: BudgetDetailFragmentArgs by navArgs()

    private lateinit var months: List<LocalDate>

    private val monthYearDateFormatter = DateTimeFormatter.ofPattern(DateFormatUtils.MMM_YYYY_DATE_FORMAT, Locale.getDefault())

    override val fragmentViewModel: BudgetDetailViewModel by viewModel { parametersOf(args.budgetId) }

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): FragmentBudgetDetailBinding {
        return FragmentBudgetDetailBinding.inflate(inflater, root, false).apply {
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

        setupLayout()
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        fragmentViewModel.transactionMonths.observe(viewLifecycleOwner) { dates ->
            months = dates.takeIf { it.isNotEmpty() } ?: listOf(LocalDate.now().withDayOfMonth(1))

            binding.mBudgetDetailViewPager.adapter = object : FragmentStateAdapter(this) {
                override fun createFragment(position: Int): Fragment {
                    return BudgetDetailMonthlyFragment.instantiate(args.budgetId, months[position])
                }

                override fun getItemCount(): Int = months.size
            }

            binding.mBudgetDetailTabs.addTabs(
                months.map { date -> monthYearDateFormatter.format(date) },
                months.indexOf(months.findLast { date ->
                    val now = LocalDate.now()

                    return@findLast date.isBefore(now) || date.isEqual(now)
                })
            )
        }

        binding.mBudgetDetailTabs.setUpWithViewPager(binding.mBudgetDetailViewPager)

        binding.mBudgetDetailTabs.addOnPageChangeListener {
            setupChart(months[it])
        }
    }

    private fun setupChart(month: LocalDate) {
        val days = Array<LocalDate>(month.lengthOfMonth()) { i -> month.plusDays(i.toLong()) }

        fragmentViewModel.transactionsByMonth(month).observe(viewLifecycleOwner) { transactions ->
            val values = transactions.groupBy { it.date }.mapValues { map ->
                map.value.sumOf { it.value }
            }

            val dataPoints = mutableListOf<DateDataPoint>()

            days.forEach { date ->
                var value = values[date]?.toFloat() ?: 0f

                if (dataPoints.isNotEmpty()) value += dataPoints.last().value

                dataPoints.add(DateDataPoint(date, value))
            }

            binding.mBudgetDetailChart.setDataPoints(dataPoints)
        }
    }

    //endregion
}