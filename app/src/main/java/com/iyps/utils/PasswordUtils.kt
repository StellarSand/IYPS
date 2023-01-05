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
            lateinit var result: String

            // Take days in:
            // Month = 31, Year = 365

            // Worst = less than/equal to 1 hour
            if (crackTimeMilliSeconds <= TimeUnit.MINUTES.toMillis(60)) {

                result = "WORST"
            }
            // Weak = more than 1 hour, less than/equal to 31 days
            else if (crackTimeMilliSeconds > TimeUnit.MINUTES.toMillis(60)
                && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(31)) {

                result = "WEAK"
            }
            // Medium = more than 31 days, less than/equal to 6 months
            else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(31)
                && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(186)) {

                result = "MEDIUM"
            }
            // Strong = more than 6 months, less than/equal to 5 years
            else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(186)
                && crackTimeMilliSeconds <= TimeUnit.DAYS.toMillis(1825)) {

                result = "STRONG"
            }
            // Excellent = more than 5 years
            else if (crackTimeMilliSeconds > TimeUnit.DAYS.toMillis(1825)) {

                result = "EXCELLENT"
            }

            return result
        }

        // Replace hardcoded strings from the library for proper language support
        // Since devs of zxcvbn4j won't fix it, we do it ourselves
        fun replaceStrings(timeToCrackString: String, fragment: Fragment): String{
            var timeString = timeToCrackString

            when {

                timeString.contains("less than a second") -> timeString = timeString
                    .replace("less than a second", fragment.getString(R.string.less_than_sec))

                timeString.contains("second") -> timeString = timeString
                    .replace("second", fragment.getString(R.string.second))

                timeString.contains("minute") -> timeString = timeString
                    .replace("minute", fragment.getString(R.string.minute))

                timeString.contains("hour") -> timeString = timeString
                    .replace("hour", fragment.getString(R.string.hour))

                timeString.contains("day") -> timeString = timeString
                    .replace("day", fragment.getString(R.string.day))

                timeString.contains("month") -> timeString = timeString
                    .replace("month", fragment.getString(R.string.month))

                timeString.contains("year") -> timeString = timeString
                    .replace("year", fragment.getString(R.string.year))

                timeString.contains("centuries") -> timeString = timeString
                    .replace("centuries", fragment.getString(R.string.centuries))

            }

            return timeString
        }

    }

}