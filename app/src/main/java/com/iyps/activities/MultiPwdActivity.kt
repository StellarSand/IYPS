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

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.iyps.R
import com.iyps.databinding.ActivityMultiPwdBinding
import com.iyps.objects.MultiPwdList
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.BLOCK_SS
import com.iyps.preferences.PreferenceManager.Companion.GRID_VIEW
import com.iyps.preferences.PreferenceManager.Companion.SORT_ASC
import com.iyps.utils.UiUtils.Companion.blockScreenshots
import com.iyps.utils.UiUtils.Companion.setButtonTooltipText
import com.iyps.utils.UiUtils.Companion.setNavBarContrastEnforced
import org.koin.android.ext.android.inject
import kotlin.getValue

class MultiPwdActivity : AppCompatActivity() {
    
    private lateinit var navController: NavController
    private val prefManager by inject<PreferenceManager>()
    var isGridView = false
    var isAscSort = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.setNavBarContrastEnforced()
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        val activityBinding = ActivityMultiPwdBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.multi_pwd_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        isGridView = prefManager.getBoolean(GRID_VIEW, defValue = false)
        isAscSort = prefManager.getBoolean(SORT_ASC)
        
        // Disable screenshots and screen recordings
        window.blockScreenshots(prefManager.getBoolean(BLOCK_SS))
        
        // Back
        activityBinding.backButton.apply {
            setButtonTooltipText(getString(R.string.back))
            setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
        
        // View
        activityBinding.viewButton.apply {
            setButtonTooltipText(getString(R.string.view))
            setViewButtonIcon()
            setOnClickListener {
                isGridView = !isGridView
                setViewButtonIcon()
                navController.navigate(R.id.action_multiPwdFragment_self)
            }
        }
        
        // Sort
        activityBinding.sortButton.apply {
            setButtonTooltipText(getString(R.string.sort))
            setOnClickListener {
                isAscSort = !isAscSort
                navController.navigate(R.id.action_multiPwdFragment_self)
            }
        }
    }
    
    private fun MaterialButton.setViewButtonIcon() {
        icon = ContextCompat.getDrawable(this@MultiPwdActivity,
                                         if (!isGridView) R.drawable.ic_view_grid
                                         else R.drawable.ic_view_list)
    }
    
    // On back pressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        prefManager.apply {
            setBoolean(GRID_VIEW, isGridView)
            setBoolean(SORT_ASC, isAscSort)
        }
        MultiPwdList.pwdList.clear()
    }
}