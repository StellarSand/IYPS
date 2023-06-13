/*
 * Copyright (c) 2022 the-weird-aquarian
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

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.iyps.R
import com.iyps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private var selectedItem = 0
    
    companion object {
        const val READ_FILE_REQ_CODE = 1000
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
    
        navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController
        
        /*########################################################################################*/
        
        // Disable screenshots and screen recordings
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    
        selectedItem = savedInstanceState?.getInt("selectedItem") ?: R.id.password
        
        // On click tab
        activityBinding.bottomNav.apply {
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
            mapOf(Pair(R.id.fileFragment, R.id.password) to R.id.action_fileFragment_to_passwordFragment,
                  Pair(R.id.aboutFragment, R.id.password) to R.id.action_aboutFragment_to_passwordFragment,
                  Pair(R.id.passwordFragment, R.id.file) to R.id.action_passwordFragment_to_fileFragment,
                  Pair(R.id.aboutFragment, R.id.file) to R.id.action_aboutFragment_to_fileFragment,
                  Pair(R.id.passwordFragment, R.id.about) to R.id.action_passwordFragment_to_aboutFragment,
                  Pair(R.id.fileFragment, R.id.about) to R.id.action_fileFragment_to_aboutFragment)
        
        val action = actionsMap[Pair(currentFragment.id, clickedItem)] ?: 0
        
        // java.lang.IllegalArgumentException:
        // Destination id == 0 can only be used in conjunction with a valid navOptions.popUpTo
        // Hence the second check
        if (clickedItem != currentFragment.id && action != 0) {
            activityBinding.bottomNav.menu.findItem(selectedItem).isChecked = true
            navController.navigate(action)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedItem", selectedItem)
    }
    
    
    // Manage deny and don't ask again in read external storage permission
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String?>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == READ_FILE_REQ_CODE) {
            
            if (grantResults[0] == PERMISSION_DENIED) {
                
                // On click deny
                if (ActivityCompat
                        .shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                    
                    Snackbar.make(activityBinding.coordinatorLayout,
                                  getString(R.string.read_files_perm_denied),
                                  BaseTransientBottomBar.LENGTH_SHORT)
                        .setAnchorView(activityBinding.selectButton)
                        .show()
                }
                // On click deny and don't ask again
                else {
                    Snackbar.make(activityBinding.coordinatorLayout,
                                  getString(R.string.read_files_perm_blocked),
                                  BaseTransientBottomBar.LENGTH_LONG)
                        .setAnchorView(activityBinding.selectButton)
                        // On click enable button, show app info in device settings
                        .setAction(getString(R.string.enable)) {
                            startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                                              .setData(Uri.parse("package:$packageName")))
                        }
                        .show()
                }
            }
        }
    }
    
}