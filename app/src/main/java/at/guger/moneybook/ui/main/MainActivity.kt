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

package at.guger.moneybook.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.activity.BaseActivity
import at.guger.moneybook.core.ui.dialog.BottomNavigationViewDialog
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Main activity class for all content fragments.
 */
class MainActivity : BaseActivity(), Toolbar.OnMenuItemClickListener, NavController.OnDestinationChangedListener {

    //region Variables

    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    //endregion

    //region Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mBottomAppBar)

        mBottomAppBar.setNavigationOnClickListener { BottomNavigationViewDialog(R.menu.menu_nav, navController).show(supportFragmentManager, null) }
        mBottomAppBar.setOnMenuItemClickListener(this)

        navController.addOnDestinationChangedListener(this)
    }


    //endregion

    //region Menu

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        prepareMenu(mBottomAppBar)

        return super.onCreateOptionsMenu(menu)
    }

    //endregion

    //region Methods

    private fun prepareMenu(bottomAppBar: BottomAppBar) {
        val menuId = when (navController.currentDestination?.id) {
            R.id.homeFragment -> R.menu.menu_main
            else -> -1
        }

        if (menuId > 0) bottomAppBar.replaceMenu(menuId) else bottomAppBar.menu.clear()
    }

    //endregion

    //region Callback

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.actionSearch -> true
            else -> false
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        prepareMenu(mBottomAppBar)
    }

    //endregion
}
