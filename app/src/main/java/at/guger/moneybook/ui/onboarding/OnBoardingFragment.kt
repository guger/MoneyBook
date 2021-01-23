/*
 *
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
 *
 *
 */

package at.guger.moneybook.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.core.ui.fragment.BaseViewBindingFragment
import at.guger.moneybook.core.util.permissions.MaterialAlertDialogRationale
import at.guger.moneybook.data.provider.legacy.model.Category
import at.guger.moneybook.databinding.FragmentOnboardingBinding
import at.guger.moneybook.util.migration.MigrationHelper
import com.afollestad.assent.Permission
import com.afollestad.assent.askForPermissions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private fun startMigration() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.OnBoardingMigrationTo20)
            .setMessage(R.string.OnBoardingMigratingBookEntries)
            .setPositiveButton(R.string.Next) { _, _ ->
                chooseCategories()
            }
            .setCancelable(false)
            .show()
    }

    private fun chooseCategories() = GlobalScope.launch(Dispatchers.Main) {
        val categories = migrationHelper.getUsedCategories()

        when {
            categories == null -> {
                migrationHelper.finishMigration()
                close()
            }
            categories.isNotEmpty() -> {
                val categoriesArray = categories.map { it.name }.toTypedArray()

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.OnBoardingMigrationCategoriesTitle)
                    .setMultiChoiceItems(categoriesArray, null, null)
                    .setPositiveButton(R.string.Next) { dialog, _ ->
                        val checkedItemPositions = (dialog as AlertDialog).listView.checkedItemPositions

                        val checkedCategories = mutableListOf<Category>()

                        for (i in 0..categories.size) {
                            if (checkedItemPositions[i]) {
                                checkedCategories.add(categories[i])
                            }
                        }

                        migrationHelper.categories = checkedCategories

                        runMigration()
                    }
                    .setCancelable(false)
                    .show()
            }
            else -> runMigration()
        }
    }

    private fun runMigration() {
        GlobalScope.launch(Dispatchers.Main) {
            migrationHelper.migrate()

            close()

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.LetsGetStarted)
                .setMessage(R.string.MigrationCompletedSuccessfully)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setPositiveButton(R.string.OK, null)
                .show()
        }
    }

    private fun close() {
        preferences.firstStart = false
        findNavController().navigateUp()
    }

    //endregion
}