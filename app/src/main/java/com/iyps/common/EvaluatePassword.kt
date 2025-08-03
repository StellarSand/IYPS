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

import android.annotation.SuppressLint
import android.content.Context
import com.iyps.R
import com.iyps.databinding.FragmentTestPasswordBinding
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.iyps.utils.LocaleUtils.Companion.localizedFeedbackResourceBundle
import com.iyps.utils.ResultUtils
import com.nulabinc.zxcvbn.Zxcvbn
import java.util.Locale

@SuppressLint("SetTextI18n")
fun FragmentTestPasswordBinding.evaluatePassword(
    zxcvbn: Zxcvbn,
    password: CharSequence,
    context: Context,
    resultUtils: ResultUtils
) {
    
    val strength = zxcvbn.measure(password)
    val crackTimesDisplay = strength.crackTimesDisplay
    val crackTimeSeconds = strength.crackTimeSeconds
    
    val tenBCrackTimeString = crackTimesDisplay.offlineFastHashing1e10PerSecond
    val tenKCrackTimeString = crackTimesDisplay.offlineSlowHashing1e4perSecond
    val tenCrackTimeString = crackTimesDisplay.onlineNoThrottling10perSecond
    val hundredCrackTimeString = crackTimesDisplay.onlineThrottling100perHour
    
    val tenBCrackTimeMillis = (crackTimeSeconds.offlineFastHashing1e10PerSecond * 1000).toLong()
    val tenKCrackTimeMillis = (crackTimeSeconds.offlineSlowHashing1e4perSecond * 1000).toLong()
    val tenCrackTimeMillis = (crackTimeSeconds.onlineNoThrottling10perSecond * 1000).toLong()
    val hundredCrackTimeMillis = (crackTimeSeconds.onlineThrottling100perHour * 1000).toLong()
    
    // Estimated time to crack
    tenBGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(tenBCrackTimeString)
    val tenBCrackTimeScore = resultUtils.crackTimeScore(tenBCrackTimeMillis)
    resultUtils.setStrengthProgressAndText(tenBCrackTimeScore,
                                           tenBGuessesStrengthMeter,
                                           tenBGuessesStrength)
    
    tenKGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(tenKCrackTimeString)
    resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(tenKCrackTimeMillis),
                                           tenKGuessesStrengthMeter,
                                           tenKGuessesStrength)
    
    tenGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(tenCrackTimeString)
    resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(tenCrackTimeMillis),
                                           tenGuessesStrengthMeter,
                                           tenGuessesStrength)
    
    hundredGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(hundredCrackTimeString)
    resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(hundredCrackTimeMillis),
                                           hundredGuessesStrengthMeter,
                                           hundredGuessesStrength)
    
    // Warning
    val localizedFeedback = strength.feedback.withResourceBundle(localizedFeedbackResourceBundle(context))
    warningSubtitle.text = resultUtils.getWarningText(localizedFeedback, tenBCrackTimeScore)
    
    // Suggestions
    suggestionsSubtitle.text = resultUtils.getSuggestionsText(localizedFeedback)
    
    // Guesses
    val guesses = strength.guesses
    guessesSubtitle.text = resultUtils.getGuessesText(guesses)
    
    // Order of magnitude of guesses
    orderMagnSubtitle.text = strength.guessesLog10.formatToTwoDecimalPlaces()
    
    // Entropy
    val statsList = resultUtils.getStatisticsCounts(password)
    entropySubtitle.text = "${resultUtils.getEntropyText(statsList)} ${context.getString(R.string.bits)}"
    
    // Match sequence
    matchSequenceSubtitle.text = resultUtils.getMatchSequenceText(strength)
    
    // Statistics
    statsSubtitle.text =
        buildString {
            append(
                "\u2022 ${context.getString(R.string.length)}: ${String.format(Locale.getDefault(), "%d", statsList[0])}",
                "\n\u2022 ${context.getString(R.string.uppercase)}: ${String.format(Locale.getDefault(), "%d", statsList[1])}",
                "\n\u2022 ${context.getString(R.string.lowercase)}: ${String.format(Locale.getDefault(), "%d", statsList[2])}",
                "\n\u2022 ${context.getString(R.string.numbers)}: ${String.format(Locale.getDefault(), "%d", statsList[3])}",
                "\n\u2022 ${context.getString(R.string.special_char)}: ${String.format(Locale.getDefault(), "%d", statsList[4])}",
                "\n\u2022 ${context.getString(R.string.spaces)}: ${String.format(Locale.getDefault(), "%d", statsList[5])}"
            )
        }
    
}