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

package com.iyps.common

import android.content.Context
import com.iyps.R
import com.iyps.databinding.FragmentTestPasswordBinding

fun FragmentTestPasswordBinding.getFormattedResultsText(context: Context): String {
    return buildString {
        append("# ${context.getString(R.string.password)}\n")
        append("${passwordText.text.toString()}\n\n")
        append("## ${context.getString(R.string.est_time_to_crack)}\n\n")
        append("#### ${context.getString(R.string.ten_b_guesses_per_sec)}\n")
        append("\u2022 ${tenBGuessesSubtitle.text}\n")
        append("\u2022 ${context.getString(R.string.strength)}: ${tenBGuessesStrength.text}\n\n")
        append("#### ${context.getString(R.string.ten_k_guesses_per_sec)}\n")
        append("\u2022 ${tenKGuessesSubtitle.text}\n")
        append("\u2022 ${context.getString(R.string.strength)}: ${tenKGuessesStrength.text}\n\n")
        append("#### ${context.getString(R.string.ten_guesses_per_sec)}\n")
        append("\u2022 ${tenGuessesSubtitle.text}\n")
        append("\u2022 ${context.getString(R.string.strength)}: ${tenGuessesStrength.text}\n\n")
        append("#### ${context.getString(R.string.hundred_guesses_per_hour)}\n")
        append("\u2022 ${hundredGuessesSubtitle.text}\n")
        append("\u2022 ${context.getString(R.string.strength)}: ${hundredGuessesStrength.text}\n\n")
        append("## ${context.getString(R.string.warning)}\n")
        append("${warningSubtitle.text}\n\n")
        append("## ${context.getString(R.string.suggestions)}\n")
        append("${suggestionsSubtitle.text}\n\n")
        append("## ${context.getString(R.string.est_guesses_to_crack)}\n")
        append("${guessesSubtitle.text.replace(
            Regex("\u00D7\\s+10(\\d+)"), "\u00D7 10\u005E$1" // Insert ^ after "x 10" (before superscript)
        )}\n\n")
        append("## ${context.getString(R.string.order_of_magn)}\n")
        append("${orderMagnSubtitle.text}\n\n")
        append("## ${context.getString(R.string.entropy)}\n")
        append("${entropySubtitle.text}\n\n")
        append("## ${context.getString(R.string.match_sequence)}\n")
        append("${matchSequenceSubtitle.text}\n\n")
        append("## ${context.getString(R.string.statistics)}\n")
        append("${statsSubtitle.text}")
    }
}
