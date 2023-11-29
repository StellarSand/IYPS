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
import androidx.core.text.HtmlCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.iyps.R
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.nulabinc.zxcvbn.Feedback
import com.nulabinc.zxcvbn.Pattern
import com.nulabinc.zxcvbn.Strength
import java.util.concurrent.TimeUnit

class ResultUtils(val context: Context) {
    
    private val emptyMeterColor = context.resources.getColor(android.R.color.transparent, context.theme)
    private val worstMeterColor = context.resources.getColor(R.color.worstMeterColor, context.theme)
    private val weakMeterColor = context.resources.getColor(R.color.weakMeterColor, context.theme)
    private val mediumMeterColor = context.resources.getColor(R.color.mediumMeterColor, context.theme)
    private val strongMeterColor = context.resources.getColor(R.color.strongMeterColor, context.theme)
    private val excellentMeterColor = context.resources.getColor(R.color.excellentMeterColor, context.theme)
    
    private val worstString = context.getString(R.string.worst)
    private val weakString = context.getString(R.string.weak)
    private val mediumString = context.getString(R.string.medium)
    private val strongString = context.getString(R.string.strong)
    private val excellentString = context.getString(R.string.excellent)
    private val notApplicableString = context.getString(R.string.na)
    
    private val worstPassWarning = context.getString(R.string.worst_pass_warning)
    private val weakPassWarning = context.getString(R.string.weak_pass_warning)
    private val mediumPassWarning = context.getString(R.string.medium_pass_warning)
    
    // Replace hardcoded strings from the library for proper language support
    fun replaceCrackTimeStrings(timeToCrackString: String): String{
        
        val replacementStringMap =
            mapOf("less than a second" to context.getString(R.string.less_than_sec),
                  "second" to context.getString(R.string.second),
                  "minute" to context.getString(R.string.minute),
                  "hour" to context.getString(R.string.hour),
                  "day" to context.getString(R.string.day),
                  "month" to context.getString(R.string.month),
                  "year" to context.getString(R.string.year),
                  "centuries" to context.getString(R.string.centuries))
        
        var replacedString = timeToCrackString
        for ((key, value) in replacementStringMap) {
            replacedString = replacedString.replace(key, value)
        }
        
        return replacedString
    }
    
    // Check password crack time (custom result)
    fun crackTimeResult(crackTimeMilliSeconds: Long): String {
        
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
    
    fun setStrengthProgressAndText(timeToCrackResult: String,
                                   strengthMeter: LinearProgressIndicator,
                                   strengthTextView: MaterialTextView) {
        val strengthProgTextMap = mapOf("WORST" to Triple(20, worstMeterColor, worstString),
                                        "WEAK" to Triple(40, weakMeterColor, weakString),
                                        "MEDIUM" to Triple(60, mediumMeterColor, mediumString),
                                        "STRONG" to Triple(80, strongMeterColor, strongString),
                                        "EXCELLENT" to Triple(100, excellentMeterColor, excellentString))
        
        val (progress, indicatorColor, strengthText) =
            strengthProgTextMap[timeToCrackResult] ?: Triple(0,
                                                             emptyMeterColor,
                                                             notApplicableString)
        
        strengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        strengthTextView.text = strengthText
    }
    
    fun getWarningText(localizedFeedback: Feedback,
                       passwordCrackTimeResult: String): String {
        val warningText =
            localizedFeedback.warning
                .ifEmpty { // If empty, set to custom warning message
                    when (passwordCrackTimeResult) {
                        "WORST" -> worstPassWarning // Worst warning
                        "WEAK" -> weakPassWarning // Weak warning
                        "MEDIUM" -> mediumPassWarning // Medium warning
                        else -> notApplicableString // For strong & above
                    }
                }
        
        return warningText
    }
    
    fun getSuggestionsText(localizedFeedback: Feedback): CharSequence {
        val suggestions = localizedFeedback.suggestions
        val suggestionText = buildString {
            if (suggestions.isNotEmpty()) {
                suggestions.forEachIndexed { index, suggestion ->
                    append("\u2022 $suggestion")
                    if (index != suggestions.lastIndex) {
                        append("\n")
                    }
                }
            }
            else {
                notApplicableString
            }
        }
        
        return suggestionText
    }
    
    fun getGuessesText(guesses: Double): CharSequence {
        val guessesString = guesses.toString()
        val formattedGuessesString =
            if (guessesString.contains("E")) {
                val splitString = guessesString.split("E")
                HtmlCompat.fromHtml("${splitString[0].toDouble().formatToTwoDecimalPlaces()} x 10<sup><small>${splitString[1]}</small></sup>",
                                    HtmlCompat.FROM_HTML_MODE_COMPACT)
            }
            else {
                guessesString.toDouble().formatToTwoDecimalPlaces()
            }
        return formattedGuessesString
    }
    
    fun getMatchSequenceText(strength: Strength): CharSequence {
        val matchSequence = strength.sequence
        val matchesText = StringBuilder()
        matchSequence.forEachIndexed { index, match ->
            val pattern = match.pattern
            
            matchesText.apply {
                append("<b>${index + 1}) \"${match.token}\"</b>")
                append("<br>${context.getString(R.string.pattern)}: $pattern")
                if (matchSequence.size > 1)
                    append("<br>\u2022 ${context.getString(R.string.order_of_magn)}: ${match.guessesLog10.formatToTwoDecimalPlaces()}")
                
                when (pattern) {
                    Pattern.Dictionary -> {
                        append("<br>${context.getString(R.string.dict_name)}: ${match.dictionaryName}")
                        if (matchSequence.size > 1) append("<br>${context.getString(R.string.rank)}: ${match.rank}")
                        append("<br>${context.getString(R.string.reversed)}: ${match.reversed}")
                        if (match.l33t)
                            append("<br>${context.getString(R.string.substitutions)}: ${match.subDisplay.removeSurrounding("[", "]")}")
                    }
                    
                    Pattern.Repeat -> {
                        append("<br>${context.getString(R.string.base_token)}: ${match.baseToken}")
                        append("<br>${context.getString(R.string.repeat_times, match.repeatCount.toString())}")
                    }
                    
                    Pattern.Sequence -> {
                        append("<br>${context.getString(R.string.sequence_name)}: ${match.sequenceName}")
                        append("<br>${context.getString(R.string.sequence_size)}: ${match.sequenceSpace}")
                        append("<br>${context.getString(R.string.ascending)}: ${match.ascending}")
                    }
                    
                    Pattern.Date-> {
                        append("<br>${context.getString(R.string.Day)}: ${match.day}")
                        append("<br>${context.getString(R.string.Month)}: ${match.month}")
                        append("<br>${context.getString(R.string.Year)}: ${match.year}")
                        append("<br>${context.getString(R.string.separator)}: ${match.separator}")
                    }
                    
                    Pattern.Spatial -> {
                        append("<br>${context.getString(R.string.graph)}: ${match.graph}")
                        append("<br>${context.getString(R.string.turns)}: ${match.turns}")
                        append("<br>${context.getString(R.string.shifted_times, match.shiftedCount.toString())}")
                    }
                    
                    Pattern.Regex -> append("<br>${context.getString(R.string.regex_name)}: ${match.regexName}")
                    
                    else -> {}
                }
                
                if (index != matchSequence.lastIndex) append("<br>__________________________<br><br>")
            }
        }
        
        return HtmlCompat.fromHtml(matchesText.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
    
    fun getStatisticsCounts(charSequence: CharSequence): List<Int> {
        val length = charSequence.length
        val upperCaseCount = charSequence.count { it.isUpperCase() }
        val lowerCaseCount = charSequence.count { it.isLowerCase() }
        val numbersCount = charSequence.count { it.isDigit() }
        val specialCharsCount = length - upperCaseCount - lowerCaseCount - numbersCount
        return listOf(length, upperCaseCount, lowerCaseCount, numbersCount, specialCharsCount)
    }
    
}