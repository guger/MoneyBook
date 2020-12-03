/*
 *
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
 *
 *
 */

package at.guger.moneybook.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.core.ui.fragment.BaseViewBindingFragment
import at.guger.moneybook.core.util.permissions.MaterialAlertDialogRationale
import at.guger.moneybook.data.repository.AccountsRepository
import at.guger.moneybook.databinding.FragmentOnboardingBinding
import at.guger.moneybook.util.migration.MigrationHelper
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsMultiChoice
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

/**
 * OnBoarding fragment class.
 */
class OnBoardingFragment : BaseViewBindingFragment<FragmentOnboardingBinding>() {

    //region Variables

    private val preferences: Preferences by inject()

    private val accountsRepository: AccountsRepository by inject()

    private val migrationHelper by lazy { MigrationHelper(requireContext(), get(), get(), get(), get()) }

    //endregion

    //region Fragment

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean): FragmentOnboardingBinding {
        return FragmentOnboardingBinding.inflate(inflater, parent, attachToParent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {}

        binding.mOnBoardingView.apply {
            skipButtonCallback = { navigateToHomeOrMigrate() }
            startMigrateButtonCallback = {
                navigateToHomeOrMigrate()
            }
        }
    }

    //endregion

    //region Methods

    private fun navigateToHomeOrMigrate() {
        val migrate = MigrationHelper.needMigration(requireContext())

        askForPermissions(Permission.READ_CONTACTS,
            rationaleHandler = MaterialAlertDialogRationale(requireActivity(), R.string.ContactsPermission, ::askForPermissions) {
                onPermission(Permission.READ_CONTACTS, if (migrate) R.string.ContactsPermissionNeededMigration else R.string.ContactsPermissionNeeded)
            }
        ) {
            if (migrate) {
                startMigration()
            } else {
                close()
            }
        }
    }

    private fun startMigration() = GlobalScope.launch(Dispatchers.Main) {
        val categories = migrationHelper.getUsedCategories()

        when {
            categories == null -> {
                migrationHelper.finishMigration()
                close()
            }
            categories.isNotEmpty() -> {
                MaterialDialog(requireContext()).show {
                    title(text = "Which categories do you want to migrate?")
                    message(text = "Categories will be converted to budgets. Book Entries, whose categories are not migrated, will be copied uncategorized.")
                    listItemsMultiChoice(items = categories.map { it.name }, allowEmptySelection = true) { _, _, items ->
                        migrationHelper.categories = categories.filter { items.contains(it.name) }
                    }
                    positiveButton(res = R.string.Next) {
                        chooseAccount()
                    }
                    cancelOnTouchOutside(false)
                }
            }
            else -> chooseAccount()
        }
    }

    private fun chooseAccount() = GlobalScope.launch(Dispatchers.Main) {
        val accounts = accountsRepository.getAccounts()

        MaterialDialog(requireContext()).show {
            title(text = "Choose an account")
            listItemsSingleChoice(items = accounts.map { it.name }, initialSelection = 0, waitForPositiveButton = true) { _, _, text ->
                migrationHelper.account = accounts.find { it.name == text }!!
            }
            positiveButton(res = R.string.Next) {
                runMigration()
            }
            cancelOnTouchOutside(false)
        }
    }

    private fun runMigration() {
        GlobalScope.launch(Dispatchers.Main) {
            migrationHelper.migrate()

            close()

            MaterialDialog(requireContext()).show {
                icon(R.drawable.ic_launcher_foreground)
                title(res = R.string.LetsGetStarted)
                message(res = R.string.MigrationCompletedSuccessfully)
                positiveButton(R.string.OK)
            }
        }
    }

    private fun close() {
        preferences.firstStart = false
        findNavController().navigateUp()
    }

    //endregion
}