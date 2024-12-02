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

package com.iyps.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.objects.AppState

@RequiresApi(Build.VERSION_CODES.N)
class QsTileService : TileService() {
    
    override fun onStartListening() {
        super.onStartListening()
        
        // Update tile state based on app status
        val isAppOpen = AppState.isAppOpen
        
        qsTile.apply {
            state = if (isAppOpen) Tile.STATE_UNAVAILABLE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= 29) subtitle = if(isAppOpen) getString(R.string.app_in_use) else null
        }
        
        qsTile.updateTile()
    }
    
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        
        val intent =
            Intent(this, MainActivity::class.java).apply {
                putExtra("shortcut", "shortcutGenerate")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        
        if (Build.VERSION.SDK_INT >= 34) {
            startActivityAndCollapse(PendingIntent.getActivity(this,
                                                               0,
                                                               intent,
                                                               PendingIntent.FLAG_IMMUTABLE))
        }
        else {
            startActivityAndCollapse(intent)
        }
    }
    
}