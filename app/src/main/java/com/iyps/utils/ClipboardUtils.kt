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

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClipboardUtils {
    
    companion object {
        
        fun manageClipboard(clipboardManager: ClipboardManager, lifecycleScope: LifecycleCoroutineScope) {
            var job: Job? = null
            
            // Clear clipboard after 1 minute if password is copied
            clipboardManager.addPrimaryClipChangedListener {
                if (clipboardManager.hasPrimaryClip()
                    && clipboardManager.primaryClipDescription?.label == "IYPS") {
                    job?.cancel()
                    job = lifecycleScope.launch {
                        delay(60000)
                        clipboardManager.clearClipboard()
                    }
                }
            }
        }
        
        // Hide from revealing on copy
        // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#SensitiveContent
        @RequiresApi(Build.VERSION_CODES.N)
        fun ClipData.hideSensitiveContent() {
            description.extras = PersistableBundle().apply {
                if (Build.VERSION.SDK_INT >= 33) putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                else putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        
        // Clear password from clipboard
        fun ClipboardManager.clearClipboard() {
            when {
                Build.VERSION.SDK_INT >= 28 -> clearPrimaryClip()
                else -> setPrimaryClip(ClipData.newPlainText(null, null))
            }
            
        }
        
    }
}