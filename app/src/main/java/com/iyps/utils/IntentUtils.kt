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

package com.iyps.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.iyps.R

class IntentUtils {

    companion object {

        // URLs
        fun openURL(activityFrom: Activity, URL: String?) {
            try {
                activityFrom.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL)))
            }
            // If browsers not installed, show toast
            catch (e: ActivityNotFoundException) {
                Toast.makeText(activityFrom, activityFrom.resources.getString(R.string.no_browsers), Toast.LENGTH_SHORT).show()
            }
        }

    }

}