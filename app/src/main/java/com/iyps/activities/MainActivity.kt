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
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.iyps.R
import com.iyps.databinding.ActivityMainBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetThemeBinding
import com.iyps.fragments.help.AboutFragment.Companion.openURL
import com.iyps.fragments.main.FileFragment
import com.iyps.fragments.main.PasswordFragment
import com.iyps.preferences.PreferenceManager

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

        setSupportActionBar(activityBinding.toolbarMain)

        // Default fragment
        displayFragment(0)

        // On click tab
        activityBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                displayFragment(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

    }

    // Setup fragments
    fun displayFragment(tab: Int) {

        val transaction = supportFragmentManager.beginTransaction()

        when (tab) {

            0 -> {
                fragment = PasswordFragment()
                transaction.setCustomAnimations(R.anim.slide_from_start, R.anim.slide_to_end,
                                                R.anim.slide_from_end, R.anim.slide_to_start)
                activityBinding.selectButton.visibility = View.GONE
            }

            1 -> {
                fragment = FileFragment()
                transaction.setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                                R.anim.slide_from_start, R.anim.slide_to_end)
                activityBinding.selectButton.visibility = View.VISIBLE

            }

        }

        transaction
            .replace(R.id.activity_host_fragment, fragment)
            .commitNow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Theme
        when(item.itemId) {

            R.id.theme -> themeBottomSheet()
            R.id.report_issue -> openURL(this, "https://github.com/the-weird-aquarian/IYPS/issues")
            R.id.about -> startActivity(Intent(this, HelpActivity::class.java)
                                        .putExtra("fragment", "About"))

        }

        return true
    }

    private fun themeBottomSheet() {

        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetThemeBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Title
        headerBinding.bottomSheetTitle.setText(R.string.theme)

        // Show system default option only on SDK 29 and above
        if (Build.VERSION.SDK_INT >= 29) {
            bottomSheetBinding.optionDefault.visibility = View.VISIBLE
        }
        else {
            bottomSheetBinding.optionDefault.visibility = View.GONE
        }

        // Default checked radio
        if (preferenceManager.getInt(PreferenceManager.THEME_PREF) == 0) {

            if (Build.VERSION.SDK_INT >= 29) {
                preferenceManager.setInt(PreferenceManager.THEME_PREF, R.id.option_default)
            }
            else {
                preferenceManager.setInt(PreferenceManager.THEME_PREF, R.id.option_light)
            }

        }
        bottomSheetBinding.optionsRadiogroup.check(preferenceManager.getInt(PreferenceManager.THEME_PREF))

        bottomSheetBinding.optionsRadiogroup
            .setOnCheckedChangeListener { _, checkedId ->

                when(checkedId) {

                    R.id.option_default -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    R.id.option_light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    R.id.option_dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

                }

                preferenceManager.setInt(PreferenceManager.THEME_PREF, checkedId)
                bottomSheetDialog.dismiss()
                recreate()
            }

        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { bottomSheetDialog.cancel() }
        bottomSheetDialog.show()
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
                // On click deny adn don't ask again
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