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
import com.google.android.material.slider.Slider
import com.iyps.R

class UiUtils {
    
    companion object {
        
        fun setSliderThumbColor(context: Context,
                                slider: Slider,
                                minValue: Float,
                                currentValue: Float) {
            val sliderThumbColor =
                if (currentValue == minValue) {
                    context.getColor(R.color.color_primary)
                }
                else {
                    context.getColor(R.color.color_onPrimary)
                }
            
            slider.thumbTintList = ColorStateList.valueOf(sliderThumbColor)
        }
        
    }
}