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

import androidx.fragment.app.Fragment
import com.iyps.R
import java.util.concurrent.TimeUnit

class PasswordUtils {
    
    companion object {
        
        // Check password crack time (custom result)
        fun passwordCrackTimeResult(crackTimeMilliSeconds: Long): String {
            
            // Take days in:
            // Month = 31, Year = 365
            val result: String =
                when {
                    // Worst = less than/equal to 1 hour
                    crackTimeMilliSeconds <= TimeUnit.MINUTES.toMillis(60) -> "WORST"
                    
                    // Weak = more than 1 hour, less than/equal to 31 days
                    crackTimeMilliSeconds > TimeUnit.MINUTES.toMillis(60)
                    && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(31) -> "WEAK"
    
                    // Medium = more than 31 days, less than/equal to 6 months
                    crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(31)
                    && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(186) -> "MEDIUM"
    
                    // Strong = more than 6 months, less than/equal to 5 years
                    crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(186)
                    && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(1825) -> "STRONG"
    
                    // Excellent = more than 5 years
                    else -> "EXCELLENT"
                }
            
            return result
        }
        
        // Replace hardcoded strings from the library for proper language support
        // Since devs of zxcvbn4j won't fix it, we do it ourselves
        fun replaceStrings(timeToCrackString: String, fragment: Fragment): String{
            
            val replacementStringMap =
                mapOf("less than a second" to fragment.getString(R.string.less_than_sec),
                      "second" to fragment.getString(R.string.second),
                      "minute" to fragment.getString(R.string.minute),
                      "hour" to fragment.getString(R.string.hour),
                      "day" to fragment.getString(R.string.day),
                      "month" to fragment.getString(R.string.month),
                      "year" to fragment.getString(R.string.year),
                      "centuries" to fragment.getString(R.string.centuries))
            
            for ((key, value) in replacementStringMap) {
                timeToCrackString.apply {
                    if (contains(key)) {
                        replace(key, value)
                    }
                }
            }
            
            return timeToCrackString
        }
        
    }
    
}