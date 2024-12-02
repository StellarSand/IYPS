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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.iyps.R
import com.iyps.utils.UiUtils.Companion.showSnackbar

class IntentUtils {

    companion object {
    
        fun openURL(activity: Activity,
                    url: String,
                    coordinatorLayout: CoordinatorLayout,
                    anchorView: View?) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            // If no browser installed, show snackbar
            catch (_: ActivityNotFoundException) {
                showSnackbar(coordinatorLayout,
                             activity.getString(R.string.no_browsers),
                             anchorView)
            }
        }
        
        fun openURL(activity: Activity,
                    url: String) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            // If no browser installed, show toast
            catch (_: ActivityNotFoundException) {
                Toast.makeText(activity, activity.getString(R.string.no_browsers), Toast.LENGTH_SHORT).show()
            }
        }

    }

}