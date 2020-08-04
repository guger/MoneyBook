/*
 *
 *  * Copyright 2020 Daniel Guger
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package at.guger.moneybook.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.fragment.BaseFragment
import at.guger.moneybook.data.migration.MigrationHelper
import kotlinx.android.synthetic.main.fragment_onboarding.*

/**
 * OnBoarding fragment class.
 */
class OnBoardingFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {}

        with(mOnBoardingView) {
            skipButtonCallback = { navigateToHomeOrMigrate() }
            startMigrateButtonCallback = { navigateToHomeOrMigrate() }
        }
    }

    private fun navigateToHomeOrMigrate() {
        if (MigrationHelper.needMigration()) {
            Toast.makeText(requireContext(), "Migrate", Toast.LENGTH_LONG).show()
        } else {
            findNavController().navigateUp()
        }
    }
}