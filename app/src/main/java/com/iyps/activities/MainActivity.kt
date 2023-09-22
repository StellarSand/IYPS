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

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.iyps.R
import com.iyps.databinding.ActivityMainBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.BLOCK_SS

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var selectedItem = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
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
        
        // On click tab
        activityBinding.mainBottomNav.apply {
            menu.findItem(selectedItemId).isChecked = true
            
            setOnItemSelectedListener { item ->
                selectedItem = item.itemId
                displayFragment(selectedItem)
                true
                
            }
            
            setOnItemReselectedListener {}
        }
        
    }
    
    // Setup fragments
    private fun displayFragment(clickedItem: Int) {
        val currentFragment = navController.currentDestination!!
        
        val actionsMap =
            mapOf(Pair(R.id.fileFragment, R.id.nav_password) to R.id.action_fileFragment_to_passwordFragment,
                  Pair(R.id.settingsFragment, R.id.nav_password) to R.id.action_settingsFragment_to_passwordFragment,
                  Pair(R.id.passwordFragment, R.id.nav_file) to R.id.action_passwordFragment_to_fileFragment,
                  Pair(R.id.settingsFragment, R.id.nav_file) to R.id.action_settingsFragment_to_fileFragment,
                  Pair(R.id.passwordFragment, R.id.nav_settings) to R.id.action_passwordFragment_to_settingsFragment,
                  Pair(R.id.fileFragment, R.id.nav_settings) to R.id.action_fileFragment_to_settingsFragment)
        
        val action = actionsMap[Pair(currentFragment.id, clickedItem)] ?: 0
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (clickedItem != currentFragment.id && action != 0) {
            activityBinding.mainBottomNav.menu.findItem(clickedItem).isChecked = true
            navController.navigate(action)
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
            }
            else {
                finish()
            }
        }
    }
    
}