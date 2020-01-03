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
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.ui.activity.BaseActivity
import at.guger.moneybook.core.ui.dialog.BottomNavigationViewDialog
import at.guger.moneybook.util.NavUtils
import com.afollestad.materialcab.CabApply
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.google.android.material.bottomappbar.BottomAppBar
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.colorAttr

/**
 * Main activity class for all content fragments.
 */
class MainActivity : BaseActivity(), Toolbar.OnMenuItemClickListener, NavController.OnDestinationChangedListener {

    //region Variables

    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    private val topLevelDestinations = setOf(R.id.homeFragment, R.id.settingsFragment, R.id.addEditTransactionFragment, R.id.addEditAccountBottomSheetDialogFragment)

    var cabEnabled: Boolean = true
        set(value) {
            field = value

            if (mCab.isActive()) mCab.destroy()
        }

    var mCab: AttachedCab? = null

    //endregion

    //region Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mBottomAppBar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mBottomAppBar.setOnMenuItemClickListener(this)
        mBottomAppBar.setNavigationOnClickListener {
            if (NavUtils.matchDestinations(navController.currentDestination!!, topLevelDestinations)) {
                BottomNavigationViewDialog(R.menu.menu_nav, navController).show(supportFragmentManager, null)
            } else {
                navController.navigateUp()
            }
        }

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

    fun attachCab(@MenuRes menuRes: Int, exec: CabApply) {
        if (!cabEnabled) return

        mCab = createCab(R.id.mCabStub) {
            menu(menuRes)
            popupTheme(R.style.ThemeOverlay_MaterialComponents_Dark)
            backgroundColor(literal = colorAttr(R.attr.colorSurface))
            closeDrawable(R.drawable.ic_close)
            fadeIn()

            exec()
        }
    }

    fun destroyCab() {
        mCab.destroy()
    }

    /**
     * Workaround for using a [BottomNavigationViewDialog], since navigation component suppresses the hamburger icon when there's no drawer layout.
     */
    private fun prepareAppBar(destination: NavDestination) {
        when (destination.id) {
            R.id.addEditTransactionFragment -> {
                mBottomAppBar.performHide()
                mBottomAppBar.visibility = View.GONE
            }
            else -> {
                mBottomAppBar.visibility = View.VISIBLE
                mBottomAppBar.performShow()

                if (NavUtils.matchDestinations(destination, topLevelDestinations)) {
                    mBottomAppBar.setNavigationIcon(R.drawable.ic_menu)
                } else {
                    mBottomAppBar.setNavigationIcon(R.drawable.ic_back)
                }
            }
        }
    }

    private fun prepareMenu(bottomAppBar: BottomAppBar) {
        val menuId = when (navController.currentDestination?.id) {
            R.id.homeFragment -> R.menu.menu_main
            else -> -1
        }

        if (menuId > 0) bottomAppBar.replaceMenu(menuId) else bottomAppBar.menu.clear()
    }

    //endregion

    //region Callback

    override fun onBackPressed() {
        when {
            mCab.isActive() -> mCab.destroy()
            else -> super.onBackPressed()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.actionAddAccount -> {
                navController.navigate(MainNavDirections.actionGlobalAddEditAccountBottomSheetDialogFragment())
                true
            }
            R.id.actionAddBudget -> {
                navController.navigate(MainNavDirections.actionGlobalAddEditBudgetBottomSheetDialogFragment())
                true
            }
            R.id.actionSearch -> true
            else -> false
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        prepareAppBar(destination)
        prepareMenu(mBottomAppBar)

        invalidateOptionsMenu()
    }

    //endregion
}
