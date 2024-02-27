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

package com.iyps.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.iyps.R

class UiUtils {
    
    companion object {
        
        fun setAppTheme(selectedTheme: Int) {
            when(selectedTheme) {
                0 -> {
                    if (Build.VERSION.SDK_INT >= 29){
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                    else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                R.id.followSystem -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                R.id.light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                R.id.dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        
        fun blockScreenshots(activity: Activity,
                             shouldBlock: Boolean) {
            if (shouldBlock) {
                activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                         WindowManager.LayoutParams.FLAG_SECURE)
            }
            else {
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        
        fun showSnackbar(coordinatorLayout: CoordinatorLayout,
                         message: String,
                         anchorView: View?) {
            Snackbar.make(coordinatorLayout,
                          message,
                          BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(anchorView) // Above FAB, bottom bar etc.
                .show()
        }
        
    }
}