/*
 *     Copyright (C) 2022-present StellarSand
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.iyps.activities

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.DynamicColors
import com.iyps.R
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.ActivityMainBinding
import com.iyps.objects.AppState
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.BLOCK_SS
import com.iyps.preferences.PreferenceManager.Companion.GEN_TOGGLE
import com.iyps.preferences.PreferenceManager.Companion.MATERIAL_YOU
import com.iyps.utils.UiUtils.Companion.blockScreenshots
import com.iyps.utils.UiUtils.Companion.convertDpToPx
import com.iyps.utils.UiUtils.Companion.setNavBarContrastEnforced
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private val prefManager by inject<PreferenceManager>()
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var viewsToAnimate: Array<ViewGroup>
    private var selectedItem = 0
    
    private companion object {
        private val SHOW_ANIM_INTERPOLATOR = DecelerateInterpolator()
        private val HIDE_ANIM_INTERPOLATOR = AccelerateInterpolator()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        // Material you
        // set this here instead of in Application class,
        // or else Dynamic Colors will not be applied to this activity
        if (prefManager.getBoolean (MATERIAL_YOU, defValue = false)) {
            DynamicColors.applyToActivityIfAvailable(this)
            DynamicColors.applyToActivitiesIfAvailable(applicationContext as ApplicationManager) // For other activities
        }
        enableEdgeToEdge()
        setNavBarContrastEnforced(window)
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        AppState.isAppOpen = true
        val checkIcon = ContextCompat.getDrawable(this, R.drawable.ic_done)
        viewsToAnimate = arrayOf(activityBinding.generateToggleGroup, activityBinding.generateBottomAppBar)
        
        // Adjust UI components for edge to edge
        mapOf(activityBinding.generateBottomAppBar to 80f,
              activityBinding.generateToggleGroup to 95f).forEach { (view, margin) ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin =
                        windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom +
                        convertDpToPx(this@MainActivity, margin)
                }
                WindowInsetsCompat.CONSUMED
            }
        }
        
        // Disable screenshots and screen recordings
        blockScreenshots(this, prefManager.getBoolean(BLOCK_SS))
        
        selectedItem =
                savedInstanceState?.getInt("selectedItem") ?:
                if (intent.extras?.getString("shortcut") == "shortcutGenerate") {
                    R.id.nav_generate
                }
                else R.id.nav_test
        
        // Opened from shortcut or quick settings toggle
        if (savedInstanceState == null && selectedItem == R.id.nav_generate) {
            displayFragment(selectedItem)
            showViewsWithAnimation()
        }
        
        // Bottom nav
        activityBinding.mainBottomNav.apply {
            
            setOnItemSelectedListener { item ->
                selectedItem = item.itemId
                displayFragment(selectedItem)
                if (selectedItem == R.id.nav_generate) {
                    showViewsWithAnimation()
                }
                else {
                    hideViewsWithAnimation()
                }
                true
                
            }
            
            setOnItemReselectedListener {}
        }
        
        // Toggle button group
        activityBinding.generateToggleGroup.apply {
            val selectedToggle: Int
            prefManager.apply {
                if (getInt(GEN_TOGGLE) == 0) {
                    setInt(GEN_TOGGLE, R.id.togglePassword)
                }
                selectedToggle = getInt(GEN_TOGGLE)
                check(selectedToggle)
            }
            findViewById<MaterialButton>(selectedToggle).icon = checkIcon
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    findViewById<MaterialButton>(checkedId).icon = checkIcon // Add check icon
                    displayFragment(R.id.nav_generate, checkedId)
                    prefManager.setInt(GEN_TOGGLE, checkedId)
                }
                else {
                    findViewById<MaterialButton>(checkedId).icon = null // Remove check icon
                }
            }
        }
        
    }
    
    // Setup fragments
    private fun displayFragment(clickedNavItem: Int, clickedToggleItem: Int = prefManager.getInt(GEN_TOGGLE)) {
        val currentFragment = navController.currentDestination!!
        
        val navActionsMap =
            mapOf(Pair(R.id.generatePasswordFragment, R.id.nav_test) to R.id.action_generatePasswordFragment_to_passwordFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_test) to R.id.action_generatePassphraseFragment_to_passwordFragment,
                  Pair(R.id.settingsFragment, R.id.nav_test) to R.id.action_settingsFragment_to_passwordFragment,
                  Pair(R.id.passwordFragment, R.id.nav_settings) to R.id.action_passwordFragment_to_settingsFragment,
                  Pair(R.id.generatePasswordFragment, R.id.nav_settings) to R.id.action_generatePasswordFragment_to_settingsFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_settings) to R.id.action_generatePassphraseFragment_to_settingsFragment)
        
        val toggleActionsMap =
            mapOf(Pair(R.id.passwordFragment, R.id.togglePassword) to R.id.action_passwordFragment_to_generatePasswordFragment,
                  Pair(R.id.settingsFragment, R.id.togglePassword) to R.id.action_settingsFragment_to_generatePasswordFragment,
                  Pair(R.id.passwordFragment, R.id.togglePassphrase) to R.id.action_passwordFragment_to_generatePassphraseFragment,
                  Pair(R.id.settingsFragment, R.id.togglePassphrase) to R.id.action_settingsFragment_to_generatePassphraseFragment,
                  Pair(R.id.generatePasswordFragment, R.id.togglePassphrase) to R.id.action_generatePasswordFragment_to_generatePassphraseFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.togglePassword) to R.id.action_generatePassphraseFragment_to_generatePasswordFragment)
        
        val action =
            if (clickedNavItem == R.id.nav_generate) {
                toggleActionsMap[Pair(currentFragment.id, clickedToggleItem)] ?: 0
            }
            else {
                navActionsMap[Pair(currentFragment.id, clickedNavItem)] ?: 0
            }
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (clickedNavItem != currentFragment.id && action != 0) {
            activityBinding.mainBottomNav.menu.findItem(clickedNavItem).isChecked = true
            navController.navigate(action)
        }
    }
    
    private fun showViewsWithAnimation() {
        viewsToAnimate.forEach { view ->
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 500L
                interpolator = SHOW_ANIM_INTERPOLATOR
                view.isVisible = true
                start()
            }
        }
    }
    
    private fun hideViewsWithAnimation() {
        viewsToAnimate.forEach { view ->
            if (view.isVisible) {
                ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                    duration = 300L
                    interpolator = HIDE_ANIM_INTERPOLATOR
                    start()
                }.doOnEnd { view.isVisible = false }
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItem", selectedItem)
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.currentDestination !!.id != navController.graph.startDestinationId) {
                selectedItem = R.id.nav_test
                displayFragment(selectedItem)
                hideViewsWithAnimation()
            }
            else {
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        AppState.isAppOpen = false
    }
}