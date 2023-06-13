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

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
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

    }

}