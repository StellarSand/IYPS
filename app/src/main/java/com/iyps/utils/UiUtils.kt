/*
 * Copyright (c) 2022-present StellarSand
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

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class UiUtils {
    
    companion object {
        
        fun setSliderThumbColor(context: Context,
                                slider: Slider,
                                minValue: Float,
                                currentValue: Float) {
            val typedValue = TypedValue()
            
            val sliderThumbColor =
                if (currentValue == minValue) {
                    context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                    typedValue.data
                }
                else {
                    context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
                    typedValue.data
                }
            
            slider.thumbTintList = ColorStateList.valueOf(sliderThumbColor)
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