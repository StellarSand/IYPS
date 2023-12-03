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
    
    private val replaceLessThanSecondString = context.getString(R.string.less_than_sec)
    private val replaceSecondString = context.getString(R.string.second)
    private val replaceMinuteString = context.getString(R.string.minute)
    private val replaceHourString = context.getString(R.string.hour)
    private val replaceDayString = context.getString(R.string.day)
    private val replaceMonthString = context.getString(R.string.month)
    private val replaceYearString = context.getString(R.string.year)
    private val replaceCenturiesString = context.getString(R.string.centuries)
    
    private val patternString = context.getString(R.string.pattern)
    private val orderMagnString = context.getString(R.string.order_of_magn)
    private val dictNameString = context.getString(R.string.dict_name)
    private val rankString = context.getString(R.string.rank)
    private val reversedString = context.getString(R.string.reversed)
    private val substitutionsString = context.getString(R.string.substitutions)
    private val baseTokenString = context.getString(R.string.base_token)
    private val seqNameString = context.getString(R.string.sequence_name)
    private val seqSizeString = context.getString(R.string.sequence_size)
    private val ascendingString = context.getString(R.string.ascending)
    private val dayString = context.getString(R.string.Day)
    private val monthString = context.getString(R.string.Month)
    private val yearString = context.getString(R.string.Year)
    private val separatorString = context.getString(R.string.separator)
    private val graphString = context.getString(R.string.graph)
    private val turnsString = context.getString(R.string.turns)
    private val regexNameString = context.getString(R.string.regex_name)
    
    // Replace hardcoded strings from the library for proper language support
    fun replaceCrackTimeStrings(timeToCrackString: String): String {
        
        val replacementStringMap =
            mapOf("less than a second" to replaceLessThanSecondString,
                  "second" to replaceSecondString,
                  "minute" to replaceMinuteString,
                  "hour" to replaceHourString,
                  "day" to replaceDayString,
                  "month" to replaceMonthString,
                  "year" to replaceYearString,
                  "centuries" to replaceCenturiesString)
        
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
        return when {
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
        return localizedFeedback.warning
            .ifEmpty { // If empty, set to custom warning message
                when (passwordCrackTimeResult) {
                    "WORST" -> worstPassWarning // Worst warning
                    "WEAK" -> weakPassWarning // Weak warning
                    "MEDIUM" -> mediumPassWarning // Medium warning
                    else -> notApplicableString // For strong & above
                }
            }
    }
    
    fun getSuggestionsText(localizedFeedback: Feedback): CharSequence {
        return buildString {
            localizedFeedback.suggestions.apply {
                if (isNotEmpty()) {
                    forEachIndexed { index, suggestion ->
                        append("\u2022 $suggestion")
                        if (index != lastIndex) {
                            append("\n")
                        }
                    }
                }
                else {
                    append(notApplicableString)
                }
            }
        }
    }
    
    fun getGuessesText(guesses: Double): CharSequence {
        val guessesString = guesses.toString()
        return if (guessesString.contains("E")) {
            val splitString = guessesString.split("E")
            HtmlCompat.fromHtml("${splitString[0].toDouble().formatToTwoDecimalPlaces()} x 10<sup><small>${splitString[1]}</small></sup>",
                                HtmlCompat.FROM_HTML_MODE_COMPACT)
        }
        else {
            guessesString.toDouble().formatToTwoDecimalPlaces()
        }
    }
    
    fun getMatchSequenceText(strength: Strength): CharSequence {
        val matchSequence = strength.sequence
        val matchesText = StringBuilder()
        matchSequence.forEachIndexed { index, match ->
            val pattern = match.pattern
            
            matchesText.apply {
                append("<b>${index + 1}) \"${match.token}\"</b>")
                append("<br>${patternString}: $pattern")
                if (matchSequence.size > 1)
                    append("<br>\u2022 ${orderMagnString}: ${match.guessesLog10.formatToTwoDecimalPlaces()}")
                
                when (pattern) {
                    Pattern.Dictionary -> {
                        append("<br>${dictNameString}: ${match.dictionaryName}")
                        if (matchSequence.size > 1) append("<br>${rankString}: ${match.rank}")
                        append("<br>${reversedString}: ${match.reversed}")
                        if (match.l33t)
                            append("<br>${substitutionsString}: ${match.subDisplay.removeSurrounding("[", "]")}")
                    }
                    
                    Pattern.Repeat -> {
                        append("<br>${baseTokenString}: ${match.baseToken}")
                        append("<br>${context.getString(R.string.repeat_times, match.repeatCount.toString())}")
                    }
                    
                    Pattern.Sequence -> {
                        append("<br>${seqNameString}: ${match.sequenceName}")
                        append("<br>${seqSizeString}: ${match.sequenceSpace}")
                        append("<br>${ascendingString}: ${match.ascending}")
                    }
                    
                    Pattern.Date-> {
                        append("<br>${dayString}: ${match.day}")
                        append("<br>${monthString}: ${match.month}")
                        append("<br>${yearString}: ${match.year}")
                        append("<br>${separatorString}: ${match.separator}")
                    }
                    
                    Pattern.Spatial -> {
                        append("<br>${graphString}: ${match.graph}")
                        append("<br>${turnsString}: ${match.turns}")
                        append("<br>${context.getString(R.string.shifted_times, match.shiftedCount.toString())}")
                    }
                    
                    Pattern.Regex -> append("<br>${regexNameString}: ${match.regexName}")
                    
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