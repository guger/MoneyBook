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

package at.guger.moneybook.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import at.guger.moneybook.MainNavDirections
import at.guger.moneybook.R
import at.guger.moneybook.core.preferences.Preferences
import at.guger.moneybook.core.ui.activity.BaseActivity
import at.guger.moneybook.core.ui.dialog.BottomNavigationViewDialog
import at.guger.moneybook.core.util.Utils
import at.guger.moneybook.core.util.ext.colorAttr
import at.guger.moneybook.databinding.ActivityMainBinding
import at.guger.moneybook.util.CrashlyticsKeys
import at.guger.moneybook.util.NavUtils
import at.guger.moneybook.util.biometric.BiometricPromptUtils
import com.afollestad.materialcab.CabApply
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.inject

/**
 * Main activity class for all content fragments.
 */
class MainActivity : BaseActivity(), Toolbar.OnMenuItemClickListener, NavController.OnDestinationChangedListener {

    //region Variables

    private lateinit var binding: ActivityMainBinding

    private val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    private val topLevelDestinations = setOf(R.id.homeFragment, R.id.settingsFragment, R.id.addEditTransactionFragment, R.id.addEditAccountBottomSheetDialogFragment)

    private val preferences: Preferences by inject()

    private var isAuthenticating = false
    private var isAuthenticated = false

    var mCab: AttachedCab? = null

    //endregion

    //region Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLayout()

        setSupportActionBar(binding.mBottomAppBar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        binding.mBottomAppBar.apply {
            setOnMenuItemClickListener(this@MainActivity)
            setNavigationOnClickListener {
                if (NavUtils.matchDestinations(navController.currentDestination!!, topLevelDestinations)) {
                    BottomNavigationViewDialog(R.menu.menu_nav, navController).show(supportFragmentManager, null)
                } else {
                    navController.navigateUp()
                }
            }
        }

        navController.addOnDestinationChangedListener(this)
    }

    //endregion

    //region Menu

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        prepareMenu(binding.mBottomAppBar)

        return super.onCreateOptionsMenu(menu)
    }

    //endregion

    //region Methods

    private fun setupLayout() {
        val splashScreen = installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (Utils.isPie() && preferences.biometricAuth && !isAuthenticated) {
                        if (!isAuthenticating) {
                            val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(this@MainActivity) { success ->
                                if (success != null) {
                                    isAuthenticated = true
                                    content.viewTreeObserver.removeOnPreDrawListener(this)
                                } else {
                                    isAuthenticating = false
                                }
                            }
                            val promptInfo = BiometricPromptUtils.createPromptInfo(this@MainActivity)

                            biometricPrompt.authenticate(promptInfo)
                            isAuthenticating = true
                        }

                        false
                    } else {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    }
                }
            }
        )
    }

    fun attachCab(@MenuRes menuRes: Int, exec: CabApply) {
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
        with(binding) {
            when (destination.id) {
                R.id.onBoardingFragment -> {
                    mBottomAppBar.visibility = View.GONE
                }
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

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        Firebase.crashlytics.setCustomKey(CrashlyticsKeys.KEY_CURRENT_FRAGMENT, destination.displayName)

        prepareAppBar(destination)
        prepareMenu(binding.mBottomAppBar)

        invalidateOptionsMenu()
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
            R.id.actionSearch -> {
                Snackbar.make(binding.navHostFragment, R.string.FeatureInDevelopment, Snackbar.LENGTH_LONG).show()
                true
            }
            else -> false
        }
    }

    //endregion
}
