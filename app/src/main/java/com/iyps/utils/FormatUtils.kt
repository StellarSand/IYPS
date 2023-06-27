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