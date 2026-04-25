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

import android.content.Context
import android.os.Build
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.iyps.R
import com.iyps.utils.TextUtils.Companion.PHRASE_SEPARATORS
import com.iyps.utils.TextUtils.Companion.SPECIAL_CHARS
import kotlin.text.forEach

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
        
        fun Window.setNavBarContrastEnforced() {
            if (Build.VERSION.SDK_INT >= 29) {
                isNavigationBarContrastEnforced = false
            }
        }
        
        fun Window.blockScreenshots(shouldBlock: Boolean) {
            if (shouldBlock) {
                setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                         WindowManager.LayoutParams.FLAG_SECURE)
            }
            else {
                clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        
        fun convertDpToPx(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).toInt()
        }
        
        fun MaterialTextView.setGenTextWithColor(generatedString: String, isPassphrase: Boolean = false) {
            val digitColor = context.resources.getColor(R.color.color_number, context.theme)
            val specCharsColor = context.resources.getColor(R.color.color_specChars, context.theme)
            val defaultColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface)
            text =
                buildSpannedString {
                    generatedString.forEach { char ->
                        val color =
                            when {
                                char.isDigit() -> digitColor
                                char in (if (!isPassphrase) SPECIAL_CHARS else PHRASE_SEPARATORS) -> specCharsColor
                                else -> defaultColor
                            }
                        inSpans(ForegroundColorSpan(color)) {
                            append(char)
                        }
                    }
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