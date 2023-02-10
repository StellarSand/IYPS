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
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.iyps.R
import com.iyps.databinding.ActivityMainBinding
import com.iyps.fragments.help.AboutFragment
import com.iyps.fragments.main.FileFragment
import com.iyps.fragments.main.PasswordFragment
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.SEL_ITEM

class MainActivity : AppCompatActivity() {
    
    lateinit var activityBinding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var fragment: Fragment
    
    companion object {
        const val READ_FILE_REQ_CODE = 1000
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        preferenceManager = PreferenceManager(this)
        
        /*########################################################################################*/
        
        // Disable screenshots and screen recordings
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        
        // Default fragment
        if (preferenceManager.getInt(SEL_ITEM) == 0) {
            displayFragment(R.id.password)
        }
        else {
            displayFragment(preferenceManager.getInt(SEL_ITEM))
        }
        
        // On click tab
        activityBinding.bottomNav.setOnItemSelectedListener { item ->
            
            when(item.itemId) {
                
                R.id.password -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.password)
                }
                
                R.id.file -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.file)
                }
                
                R.id.about -> {
                    preferenceManager.setInt(SEL_ITEM, R.id.about)
                }
            }
            
            displayFragment(preferenceManager.getInt(SEL_ITEM))
            true
            
        }
        
        activityBinding.bottomNav.setOnItemReselectedListener {}
        
    }
    
    // Setup fragments
    private fun displayFragment(navItem: Int) {
        
        val transaction = supportFragmentManager.beginTransaction()
        
        when (navItem) {
            
            R.id.password -> {
                fragment = PasswordFragment()
                transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end,
                                                R.anim.slide_from_end, R.anim.slide_to_start)
                activityBinding.selectButton.visibility = View.GONE
            }
            
            R.id.file -> {
                fragment = FileFragment()
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end)
                activityBinding.selectButton.visibility = View.VISIBLE
                
            }
            
            R.id.about -> {
                fragment = AboutFragment()
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end)
                activityBinding.selectButton.visibility = View.GONE
                
            }
            
        }
        
        transaction
            .replace(R.id.activity_host_fragment, fragment)
            .commitNow()
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