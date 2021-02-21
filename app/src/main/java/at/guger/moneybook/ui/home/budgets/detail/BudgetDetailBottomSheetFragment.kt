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
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import at.guger.moneybook.core.ui.fragment.BaseDataBindingBottomSheetDialogFragment
import at.guger.moneybook.core.ui.viewmodel.EventObserver
import at.guger.moneybook.core.ui.widget.chart.DateDataPoint
import at.guger.moneybook.data.model.Budget
import at.guger.moneybook.data.model.Transaction
import at.guger.moneybook.databinding.DialogFragmentBudgetDetailBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate

/**
 * Fragment displaying the [transactions][Transaction] of an [account][Budget].
 */
class BudgetDetailBottomSheetFragment : BaseDataBindingBottomSheetDialogFragment<DialogFragmentBudgetDetailBinding, BudgetDetailBottomSheetViewModel>() {

    //region Variables

    private val args: BudgetDetailBottomSheetFragmentArgs by navArgs()

    override val fragmentViewModel: BudgetDetailBottomSheetViewModel by viewModel { parametersOf(args.budgetId) }

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): DialogFragmentBudgetDetailBinding {
        return DialogFragmentBudgetDetailBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentViewModel.openDetailFragment.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(BudgetDetailBottomSheetFragmentDirections.actionBudgetDetailBottomSheetFragmentToBudgetDetailFragment(args.budgetId))
        })

        setupChart(LocalDate.now().withDayOfMonth(1))
    }

    //endregion

    //region Methods

    private fun setupChart(month: LocalDate) {
        val days = Array<LocalDate>(month.lengthOfMonth()) { i -> month.plusDays(i.toLong()) }

        fragmentViewModel.transactionsByMonth(month).observe(viewLifecycleOwner) { transactions ->
            val values = transactions.groupBy { it.date }.mapValues { map ->
                map.value.sumByDouble { it.value }
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