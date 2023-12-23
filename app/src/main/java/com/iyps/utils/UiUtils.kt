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

import android.content.res.ColorStateList
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class UiUtils {
    
    companion object {
        
        fun setSliderThumbColor(slider: Slider,
                                minValue: Float,
                                currentValue: Float) {
            val sliderThumbColor =
                if (currentValue == minValue) {
                    MaterialColors.getColor(slider, com.google.android.material.R.attr.colorPrimary)
                }
                else {
                    MaterialColors.getColor(slider, com.google.android.material.R.attr.colorOnPrimary)
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