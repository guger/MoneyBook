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

package at.guger.moneybook.ui.home.budgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseDataBindingBottomSheetDialogFragment
import at.guger.moneybook.core.ui.widget.chart.BudgetDataPoint
import at.guger.moneybook.databinding.FragmentBudgetsInfoBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.Period
import kotlin.math.max

class BudgetsInfoBottomSheetDialogFragment : BaseDataBindingBottomSheetDialogFragment<FragmentBudgetsInfoBinding, BudgetsInfoViewModel>() {

    override val fragmentViewModel: BudgetsInfoViewModel by viewModel()

    override fun inflateBinding(inflater: LayoutInflater, root: ViewGroup?, attachToParent: Boolean): FragmentBudgetsInfoBinding {
        return FragmentBudgetsInfoBinding.inflate(inflater, root, false).apply {
            viewModel = fragmentViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mBudgetsInsightsTimeSpanToggleButtonGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val (start, end) = when (checkedId) {
                    R.id.btnBudgetsInfoThisMonth -> {
                        Pair(
                            LocalDate.now().withDayOfMonth(1),
                            LocalDate.now().run { withDayOfMonth(this.lengthOfMonth()) }
                        )
                    }
                    R.id.btnBudgetsInfoLastMonth -> {
                        Pair(
                            LocalDate.now().minusMonths(1).withDayOfMonth(1),
                            LocalDate.now().minusMonths(1).run { withDayOfMonth(this.lengthOfMonth()) }
                        )
                    }
                    else -> {
                        Pair(
                            LocalDate.now().withDayOfYear(1),
                            LocalDate.now().run { withDayOfYear(this.lengthOfYear()) }
                        )
                    }
                }

                showTimeSpan(start, end)
            }
        }

        binding.mBudgetsInsightsTimeSpanToggleButtonGroup.check(R.id.btnBudgetsInfoThisMonth)
    }

    private fun showTimeSpan(start: LocalDate, end: LocalDate) {
        fragmentViewModel.budgetsForTime(start, end).observe(viewLifecycleOwner) { budgets ->
            val data = mutableListOf<BudgetDataPoint>()

            val months = max(Period.between(start, end).months, 1)

            budgets.forEach {
                data.add(BudgetDataPoint(it.name, it.balance.toFloat() / months, it.budget.toFloat(), it.color))
            }

            binding.mBudgetsInfoBarChart.setData(data)
        }
    }
}