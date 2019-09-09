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

package at.guger.moneybook.core.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import at.guger.moneybook.core.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.dialog_bottom_navigation_view.*

/**
 * [BottomSheetDialogFragment] to be used as a [bottom navigation view][NavigationView].
 */
class BottomNavigationViewDialog(@MenuRes private val menuRes: Int, private val navController: NavController) : BottomSheetDialogFragment() {

    //region Variables

    private var onNavigationItemSelectedListener: NavigationView.OnNavigationItemSelectedListener? = null

    //endregion

    //region BottomSheetDialogFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_bottom_navigation_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBottomNavigationView.inflateMenu(menuRes)
        mBottomNavigationView.setupWithNavController(navController)
    }

    //endregion
}