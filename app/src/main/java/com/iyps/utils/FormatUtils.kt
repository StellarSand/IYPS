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

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class FormatUtils {
    
    companion object {
    
        // Format double to 2 decimal places after rounding off
        // and remove trailing zeros if it's a whole number
        // Example:
        // 10.32598 -> 10.33
        // 12.00 -> 12
        fun Double.formatToTwoDecimalPlaces(): String {
            val decimalFormat = DecimalFormat("#.##")
            decimalFormat.decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
            var formattedString = decimalFormat.format(this)
            if (formattedString.endsWith(".00")) {
                formattedString = formattedString.substring(0, formattedString.length - 3)
            }
            return formattedString
        }
        
    }
}