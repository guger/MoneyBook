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

package at.guger.moneybook.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import at.guger.moneybook.R

enum class OnBoardingPage(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    @StringRes val description: Int,
    @DrawableRes val image: Int
) {
    START(R.string.Welcome, R.string.YourPersonalFinanceManager, R.string.StayOnTopOfYourFinances, R.drawable.ic_launcher_foreground),
    ACCOUNTS(R.string.Accounts, R.string.TrackSpendingsWithAccounts, R.string.CreateAccountsOrganiseTransactions, R.drawable.ic_onboarding_accounts),
    DUES(R.string.Dues, R.string.KeepEyeOfDues, R.string.NeverForgetAnyDue, R.drawable.ic_onboarding_dues),
    BUDGETS(R.string.Budgets, R.string.OrganizeBudget, R.string.OrganizeInBudgets, R.drawable.ic_onboarding_budgets),
    GETSTARTED(R.string.GetStarted, R.string.StayOnTopOfYourFinances, R.string.GetStartedNow, R.drawable.ic_launcher_foreground)
}