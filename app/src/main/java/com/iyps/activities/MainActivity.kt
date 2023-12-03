/*
 * Copyright (c) 2022-present StellarSand
 *
 *  This file is part of IYPS.
 *
 *  IYPS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IYPS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IYPS.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.iyps.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.iyps.R
import com.iyps.databinding.ActivityMainBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.BLOCK_SS
import com.iyps.preferences.PreferenceManager.Companion.GEN_RADIO


class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    private var selectedItem = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        preferenceManager = PreferenceManager(this)
        
        /*########################################################################################*/
        
        // Disable screenshots and screen recordings
        if (PreferenceManager(this).getBoolean(BLOCK_SS)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE)
        }
        else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        
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
            mapOf(Pair(R.id.fileFragment, R.id.nav_password) to R.id.action_fileFragment_to_passwordFragment,
                  Pair(R.id.generatePasswordFragment, R.id.nav_password) to R.id.action_generatePasswordFragment_to_passwordFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_password) to R.id.action_generatePassphraseFragment_to_passwordFragment,
                  Pair(R.id.settingsFragment, R.id.nav_password) to R.id.action_settingsFragment_to_passwordFragment,
                  Pair(R.id.passwordFragment, R.id.nav_file) to R.id.action_passwordFragment_to_fileFragment,
                  Pair(R.id.generatePasswordFragment, R.id.nav_file) to R.id.action_generatePasswordFragment_to_fileFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_file) to R.id.action_generatePassphraseFragment_to_fileFragment,
                  Pair(R.id.settingsFragment, R.id.nav_file) to R.id.action_settingsFragment_to_fileFragment,
                  Pair(R.id.passwordFragment, R.id.nav_settings) to R.id.action_passwordFragment_to_settingsFragment,
                  Pair(R.id.fileFragment, R.id.nav_settings) to R.id.action_fileFragment_to_settingsFragment,
                  Pair(R.id.generatePasswordFragment, R.id.nav_settings) to R.id.action_generatePasswordFragment_to_settingsFragment,
                  Pair(R.id.generatePassphraseFragment, R.id.nav_settings) to R.id.action_generatePassphraseFragment_to_settingsFragment)
        
        val radioActionsMap =
            mapOf(Pair(R.id.passwordFragment, R.id.radioPassword) to R.id.action_passwordFragment_to_generatePasswordFragment,
                  Pair(R.id.fileFragment, R.id.radioPassword) to R.id.action_fileFragment_to_generatePasswordFragment,
                  Pair(R.id.settingsFragment, R.id.radioPassword) to R.id.action_settingsFragment_to_generatePasswordFragment,
                  Pair(R.id.passwordFragment, R.id.radioPassphrase) to R.id.action_passwordFragment_to_generatePassphraseFragment,
                  Pair(R.id.fileFragment, R.id.radioPassphrase) to R.id.action_fileFragment_to_generatePassphraseFragment,
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
        val fadeInRadioGroup = ObjectAnimator.ofFloat(activityBinding.generateRadioGroup, "alpha", 0f, 1f)
        fadeInRadioGroup.duration = 1500
        
        val fadeInBottomAppBar = ObjectAnimator.ofFloat(activityBinding.generateBottomAppBar, "alpha", 0f, 1f)
        fadeInBottomAppBar.duration = 1500
        
        activityBinding.generateRadioGroup.isVisible = true
        fadeInRadioGroup.start()
        activityBinding.generateBottomAppBar.isVisible = true
        fadeInBottomAppBar.start()
    }
    
    private fun hideViewsWithAnimation() {
        val fadeOutRadioGroup = ObjectAnimator.ofFloat(activityBinding.generateRadioGroup, "alpha", 1f, 0f)
        fadeOutRadioGroup.duration = 1000
        fadeOutRadioGroup.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (activityBinding.generateRadioGroup.isVisible) {
                    activityBinding.generateRadioGroup.isVisible = false
                }
            }
        })
        fadeOutRadioGroup.start()
        
        val fadeOutBottomAppBar = ObjectAnimator.ofFloat(activityBinding.generateBottomAppBar, "alpha", 1f, 0f)
        fadeOutBottomAppBar.duration = 1000
        fadeOutBottomAppBar.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (activityBinding.generateBottomAppBar.isVisible) {
                    activityBinding.generateBottomAppBar.isVisible = false
                }
            }
        })
        fadeOutBottomAppBar.start()
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