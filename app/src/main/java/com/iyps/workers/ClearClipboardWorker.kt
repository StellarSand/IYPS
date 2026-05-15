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

package com.iyps.workers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters

class ClearClipboardWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    
    private val clipboardManager = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    
    override fun doWork(): Result {
        return try {
            when {
                Build.VERSION.SDK_INT >= 28 -> clipboardManager.clearPrimaryClip()
                else -> clipboardManager.setPrimaryClip(ClipData.newPlainText(null, null))
            }
            Result.success()
        }
        catch (_: Exception) {
            Result.retry()
        }
    }
}