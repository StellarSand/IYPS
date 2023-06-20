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

package com.iyps.appmanager

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.android.material.elevation.SurfaceColors
import com.iyps.R
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.THEME_PREF

class ApplicationManager : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        val preferenceManager = PreferenceManager(this)
        
        // Theme
        when(preferenceManager.getInt(THEME_PREF)) {
            
            0 -> {
                
                if (Build.VERSION.SDK_INT >= 29){
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }
                
            }
            R.id.sys_default -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            R.id.light -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            R.id.dark -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        
        // Set status bar and navigation bar colors
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.apply {
                    statusBarColor = SurfaceColors.SURFACE_0.getColor(activity)
                    navigationBarColor = SurfaceColors.getColorForElevation(activity, 8f)
                }
            }
            
            override fun onActivityStarted(activity: Activity) {}
            
            override fun onActivityResumed(activity: Activity) {}
            
            override fun onActivityPaused(activity: Activity) {}
            
            override fun onActivityStopped(activity: Activity) {}
            
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            
            override fun onActivityDestroyed(activity: Activity) {}
        })
        
    }
    
}