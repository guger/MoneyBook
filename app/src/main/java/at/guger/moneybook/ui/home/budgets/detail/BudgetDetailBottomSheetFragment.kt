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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import at.guger.moneybook.core.ui.fragment.BaseDataBindingBottomSheetDialogFragment
import at.guger.moneybook.core.ui.widget.LineGraphChart
import at.guger.moneybook.core.util.ext.dp
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.FragmentBudgetDetailBinding
import at.guger.moneybook.util.DateFormatUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Fragment displaying the [transactions][Transaction] of an [account][Budget].
 */
class BudgetDetailBottomSheetFragment : BaseDataBindingBottomSheetDialogFragment<FragmentBudgetDetailBinding, BudgetDetailViewModel>() {

    //region Variables

    private val args: BudgetDetailBottomSheetFragmentArgs by navArgs()

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheet = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheet.behavior.peekHeight = requireContext().dp(280).toInt()

        return bottomSheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLayout()
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        fragmentViewModel.transactionMonths.observe(viewLifecycleOwner) {
            months = it

            binding.mBudgetDetailViewPager.adapter = object : FragmentStateAdapter(this) {
                override fun createFragment(position: Int): Fragment {
                    return BudgetDetailMonthlyFragment.instantiate(args.budgetId, it[position])
                }

                override fun getItemCount(): Int = it.size
            }

            binding.mBudgetDetailTabs.addTabs(
                it.map { date -> monthYearDateFormatter.format(date) },
                it.indexOf(it.findLast { date ->
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
                map.value.sumByDouble { if (it.type == Transaction.TransactionType.EARNING) it.value else -it.value }
            }

            val dataPoints = mutableListOf<LineGraphChart.DataPoint>()

            days.forEach { date ->
                var value = values[date]?.toFloat() ?: 0f

                if (dataPoints.isNotEmpty()) value += dataPoints.last().value

                dataPoints.add(LineGraphChart.DataPoint(date, value))
            }

            binding.mBudgetDetailChart.addDataPoints(dataPoints)
        }
    }

    //endregion
}