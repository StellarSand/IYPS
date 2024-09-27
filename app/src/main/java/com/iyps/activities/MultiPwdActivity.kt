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

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.iyps.R
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.ActivityMultiPwdBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.GRID_VIEW
import com.iyps.preferences.PreferenceManager.Companion.SORT_ASC
import com.iyps.utils.UiUtils.Companion.blockScreenshots

class MultiPwdActivity : AppCompatActivity(), MenuProvider {
    
    private lateinit var activityBinding: ActivityMultiPwdBinding
    private lateinit var appManager: ApplicationManager
    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager
    var isGridView = false
    var isAscSort = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        addMenuProvider(this)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        activityBinding = ActivityMultiPwdBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.multi_pwd_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        appManager = applicationContext as ApplicationManager
        preferenceManager = appManager.preferenceManager
        isGridView = preferenceManager.getBoolean(GRID_VIEW, defValue = false)
        isAscSort = preferenceManager.getBoolean(SORT_ASC)
        
        // Disable screenshots and screen recordings
        blockScreenshots(this, preferenceManager.getBoolean(PreferenceManager.BLOCK_SS))
        
        activityBinding.multiPwdBottomAppBar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
    }
    
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_multi_pwd, menu)
        menu.findItem(R.id.menu_view).apply {
            if (!isGridView) setIcon(R.drawable.ic_view_grid)
            else setIcon(R.drawable.ic_view_list)
        }
    }
    
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        
        when (menuItem.itemId) {
            
            R.id.menu_view -> {
                isGridView = !isGridView
                if (!isGridView) menuItem.setIcon(R.drawable.ic_view_grid)
                else menuItem.setIcon(R.drawable.ic_view_list)
                navController.navigate(R.id.action_multiPwdFragment_self)
            }
            
            R.id.menu_sort -> {
                isAscSort = !isAscSort
                navController.navigate(R.id.action_multiPwdFragment_self)
            }
            
        }
        
        return true
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.apply {
            setBoolean(GRID_VIEW, isGridView)
            setBoolean(SORT_ASC, isAscSort)
        }
        appManager.multiPasswordsList.clear()
    }
}