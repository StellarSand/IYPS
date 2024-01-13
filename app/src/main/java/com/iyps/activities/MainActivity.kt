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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.iyps.R
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.ActivityMainBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.BLOCK_SS
import com.iyps.preferences.PreferenceManager.Companion.GEN_RADIO
import com.iyps.utils.UiUtils.Companion.blockScreenshots

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var viewsToAnimate: List<ViewGroup>
    private var selectedItem = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        preferenceManager = (applicationContext as ApplicationManager).preferenceManager
        viewsToAnimate = listOf(activityBinding.generateRadioGroup, activityBinding.generateBottomAppBar)
        
        // Disable screenshots and screen recordings
        blockScreenshots(this, preferenceManager.getBoolean(BLOCK_SS))
        
        selectedItem = savedInstanceState?.getInt("selectedItem") ?: R.id.nav_password
        
        // Bottom nav
        activityBinding.mainBottomNav.apply {
            menu.findItem(selectedItemId).isChecked = true
            
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
        
        // Radio group
        activityBinding.generateRadioGroup.apply {
            preferenceManager.apply {
                if (getInt(GEN_RADIO) == 0) {
                    setInt(GEN_RADIO, R.id.radioPassword)
                }
                check(getInt(GEN_RADIO))
            }
            
            setOnCheckedChangeListener { _, checkedId ->
                displayFragment(R.id.nav_generate, checkedId)
                preferenceManager.setInt(GEN_RADIO, checkedId)
            }
        }
        
    }
    
    // Setup fragments
    private fun displayFragment(clickedNavItem: Int, clickedRadioItem: Int = preferenceManager.getInt(GEN_RADIO)) {
        val currentFragment = navController.currentDestination!!
        
        val navActionsMap =
            mapOf(Pair(R.id.generatePasswordFragment, R.id.nav_password) to R.id.action_generatePasswordFragment_to_passwordFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_password) to R.id.action_generatePassphraseFragment_to_passwordFragment,
                  Pair(R.id.settingsFragment, R.id.nav_password) to R.id.action_settingsFragment_to_passwordFragment,
                  Pair(R.id.passwordFragment, R.id.nav_settings) to R.id.action_passwordFragment_to_settingsFragment,
                  Pair(R.id.generatePasswordFragment, R.id.nav_settings) to R.id.action_generatePasswordFragment_to_settingsFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_settings) to R.id.action_generatePassphraseFragment_to_settingsFragment)
        
        val radioActionsMap =
            mapOf(Pair(R.id.passwordFragment, R.id.radioPassword) to R.id.action_passwordFragment_to_generatePasswordFragment,
                  Pair(R.id.settingsFragment, R.id.radioPassword) to R.id.action_settingsFragment_to_generatePasswordFragment,
                  Pair(R.id.passwordFragment, R.id.radioPassphrase) to R.id.action_passwordFragment_to_generatePassphraseFragment,
                  Pair(R.id.settingsFragment, R.id.radioPassphrase) to R.id.action_settingsFragment_to_generatePassphraseFragment,
                  Pair(R.id.generatePasswordFragment, R.id.radioPassphrase) to R.id.action_generatePasswordFragment_to_generatePassphraseFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.radioPassword) to R.id.action_generatePassphraseFragment_to_generatePasswordFragment)
        
        val action =
            if (clickedNavItem == R.id.nav_generate) {
                radioActionsMap[Pair(currentFragment.id, clickedRadioItem)] ?: 0
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
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
                view.isVisible = true
                start()
            }
        }
    }
    
    private fun hideViewsWithAnimation() {
        viewsToAnimate.forEach { view ->
            ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (view.isVisible) {
                            view.isVisible = false
                        }
                    }
                })
                start()
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
                selectedItem = R.id.nav_password
                displayFragment(selectedItem)
                hideViewsWithAnimation()
            }
            else {
                finish()
            }
        }
    }
    
}