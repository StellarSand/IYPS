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

import android.content.Context
import androidx.core.text.HtmlCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.iyps.R
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.nulabinc.zxcvbn.Feedback
import com.nulabinc.zxcvbn.Pattern
import com.nulabinc.zxcvbn.Strength
import java.util.Locale
import java.util.concurrent.TimeUnit

class ResultUtils(val context: Context) {
    
    private val dayStringResource = context.getString(R.string.day)
    private val monthStringResource = context.getString(R.string.month)
    private val yearStringResource = context.getString(R.string.year)
    
    private val timeUnitsReplacementMap =
        mapOf("second" to Pair(context.getString(R.string.second), context.getString(R.string.seconds)),
              "minute" to Pair(context.getString(R.string.minute), context.getString(R.string.minutes)),
              "hour" to Pair(context.getString(R.string.hour), context.getString(R.string.hours)),
              "day" to Pair(dayStringResource, context.getString(R.string.days)),
              "month" to Pair(monthStringResource, context.getString(R.string.months)),
              "year" to Pair(yearStringResource, context.getString(R.string.years)))
    
    private val timeStringsReplacementMap =
        mapOf("less than a second" to context.getString(R.string.less_than_sec),
              "centuries" to context.getString(R.string.centuries))
    
    private val timeStringsRegex = "(\\d+)\\s+(\\w+)".toRegex() // Insert regex meme here
    
    // Take days in:
    // Month = 31, Year = 365
    private val threeHrsInMillis = TimeUnit.HOURS.toMillis(3)
    private val oneMonthInMillis = TimeUnit.DAYS.toMillis(31)
    private val sixMonthsInMillis = TimeUnit.DAYS.toMillis(186)
    private val fiveYrsInMillis = TimeUnit.DAYS.toMillis(1825)
    
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
    private val naString = context.getString(R.string.na)
    
    private val worstPassWarning = context.getString(R.string.worst_pass_warning)
    private val weakPassWarning = context.getString(R.string.weak_pass_warning)
    private val mediumPassWarning = context.getString(R.string.medium_pass_warning)
    
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
    private val dayString =
        "\u2022 ${dayStringResource.replaceFirstChar { it.titlecase(Locale.getDefault()) }}"
    private val monthString =
        "\u2022 ${monthStringResource.replaceFirstChar { it.titlecase(Locale.getDefault()) }}"
    private val yearString =
        "\u2022 ${yearStringResource.replaceFirstChar { it.titlecase(Locale.getDefault()) }}"
    private val separatorString = context.getString(R.string.separator)
    private val graphString = context.getString(R.string.graph)
    private val turnsString = context.getString(R.string.turns)
    private val regexNameString = context.getString(R.string.regex_name)
    
    private companion object {
        const val WORST_SCORE = 1
        const val WEAK_SCORE = 2
        const val MEDIUM_SCORE = 3
        const val STRONG_SCORE = 4
        const val EXCELLENT_SCORE = 5
    }
    
    // Replace hardcoded strings from the library for proper language support
    fun replaceCrackTimeStrings(timeToCrackString: String): String {
        
        var replacedString = timeToCrackString
        
        timeStringsReplacementMap.forEach { (key, value) ->
            if (replacedString.contains(key)) {
                return replacedString.replace(key, value)
            }
        }
        
        replacedString =
            timeStringsRegex.replace(replacedString) { matchResult ->
                val quantity = matchResult.groupValues[1]
                val timeUnit = matchResult.groupValues[2]
                val baseUnit = if (timeUnit.endsWith("s")) timeUnit.dropLast(1) else timeUnit
                val replacementPair = timeUnitsReplacementMap[baseUnit] ?: Pair(timeUnit, "${timeUnit}s")
                "$quantity ${if (quantity == "1") replacementPair.first else replacementPair.second}"
            }
        
        return replacedString
    }
    
    // Custom score
    fun crackTimeScore(crackTimeMilliSeconds: Long): Int {
        return when (crackTimeMilliSeconds) {
            in 0..threeHrsInMillis -> WORST_SCORE
            in (threeHrsInMillis + 1)..oneMonthInMillis -> WEAK_SCORE
            in (oneMonthInMillis + 1)..sixMonthsInMillis -> MEDIUM_SCORE
            in (sixMonthsInMillis + 1)..fiveYrsInMillis -> STRONG_SCORE
            else -> EXCELLENT_SCORE
        }
    }
    
    fun setStrengthProgressAndText(crackTimeScore: Int,
                                   strengthMeter: LinearProgressIndicator,
                                   strengthTextView: MaterialTextView) {
        val strengthProgTextMap = mapOf(WORST_SCORE to Triple(20, worstMeterColor, worstString),
                                        WEAK_SCORE to Triple(40, weakMeterColor, weakString),
                                        MEDIUM_SCORE to Triple(60, mediumMeterColor, mediumString),
                                        STRONG_SCORE to Triple(80, strongMeterColor, strongString),
                                        EXCELLENT_SCORE to Triple(100, excellentMeterColor, excellentString))
        
        val (progress, indicatorColor, strengthText) =
            strengthProgTextMap[crackTimeScore] ?: Triple(0, emptyMeterColor, naString)
        
        strengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        strengthTextView.text = strengthText
    }
    
    fun getWarningText(localizedFeedback: Feedback,
                       crackTimeScore: Int): String {
        return localizedFeedback.warning
            .ifEmpty { // If empty, set to custom warning message
                when (crackTimeScore) {
                    WORST_SCORE -> worstPassWarning
                    WEAK_SCORE -> weakPassWarning
                    MEDIUM_SCORE -> mediumPassWarning
                    else -> naString
                }
            }
    }
    
    fun getSuggestionsText(localizedFeedback: Feedback): CharSequence {
        return buildString {
            localizedFeedback.suggestions.joinTo(this, "\n") { "\u2022 $it" }
                .ifEmpty { append(naString) }
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
        
        val matchesText = buildString {
            matchSequence.forEachIndexed { index, match ->
                val pattern = match.pattern
                
                append("<b>${index + 1}) \"${match.token}\"</b>")
                append("<br>${patternString}: $pattern")
                if (matchSequence.size > 1)
                    append("<br>\u2022 ${orderMagnString}: ${match.guessesLog10.formatToTwoDecimalPlaces()}")
                
                when (pattern) {
                    Pattern.Dictionary -> {
                        val dictionaryName =
                            when (match.dictionaryName) {
                                "english_wikipedia" -> "wikipedia"
                                "female_names" -> "names"
                                else -> match.dictionaryName
                            }
                        append("<br>${dictNameString}: ${dictionaryName}")
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
                        match.separator.takeIf { it.isNotEmpty() }?.let {
                            append("<br>$separatorString: $it")
                        }
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
        
        return HtmlCompat.fromHtml(matchesText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
    
    fun getStatisticsCounts(charSequence: CharSequence): List<Int> {
        val length = charSequence.length
        val upperCaseCount = charSequence.count { it.isUpperCase() }
        val lowerCaseCount = charSequence.count { it.isLowerCase() }
        val numbersCount = charSequence.count { it.isDigit() }
        val spacesCount = charSequence.count { it.isWhitespace() }
        val specialCharsCount = length - upperCaseCount - lowerCaseCount - numbersCount - spacesCount
        return listOf(length, upperCaseCount, lowerCaseCount, numbersCount, specialCharsCount, spacesCount)
    }
    
}