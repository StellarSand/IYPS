package com.iyps.common

import android.annotation.SuppressLint
import android.content.Context
import com.iyps.R
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.iyps.utils.LocaleUtils
import com.iyps.utils.ResultUtils
import com.nulabinc.zxcvbn.Zxcvbn
import kotlin.math.log2

class EvaluatePassword(zxcvbn: Zxcvbn,
                       password: CharSequence,
                       fragmentPasswordBinding: FragmentPasswordBinding,
                       context: Context,
                       resultUtils: ResultUtils) {
    
    init {
        
        val strength = zxcvbn.measure(password)
        
        val tenBCrackTimeString = strength.crackTimesDisplay.offlineFastHashing1e10PerSecond
        val tenKCrackTimeString = strength.crackTimesDisplay.offlineSlowHashing1e4perSecond
        val tenCrackTimeString = strength.crackTimesDisplay.onlineNoThrottling10perSecond
        val hundredCrackTimeString = strength.crackTimesDisplay.onlineThrottling100perHour
        
        val tenBCrackTimeMilliSeconds =
            (strength.crackTimeSeconds.offlineFastHashing1e10PerSecond * 1000).toLong()
        val tenKCrackTimeMilliSeconds =
            (strength.crackTimeSeconds.offlineSlowHashing1e4perSecond * 1000).toLong()
        val tenCrackTimeMilliSeconds =
            (strength.crackTimeSeconds.onlineNoThrottling10perSecond * 1000).toLong()
        val hundredCrackTimeMilliSeconds =
            (strength.crackTimeSeconds.onlineThrottling100perHour * 1000).toLong()
        
        // Estimated time to crack
        fragmentPasswordBinding.tenBGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(tenBCrackTimeString)
        val tenBCrackTimeResult = resultUtils.crackTimeResult(tenBCrackTimeMilliSeconds)
        resultUtils.setStrengthProgressAndText(tenBCrackTimeResult,
                                               fragmentPasswordBinding.tenBGuessesStrengthMeter,
                                               fragmentPasswordBinding.tenBGuessesStrength)
        
        fragmentPasswordBinding.tenKGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(tenKCrackTimeString)
        resultUtils.setStrengthProgressAndText(resultUtils.crackTimeResult(tenKCrackTimeMilliSeconds),
                                               fragmentPasswordBinding.tenKGuessesStrengthMeter,
                                               fragmentPasswordBinding.tenKGuessesStrength)
        
        fragmentPasswordBinding.tenGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(tenCrackTimeString)
        resultUtils.setStrengthProgressAndText(resultUtils.crackTimeResult(tenCrackTimeMilliSeconds),
                                               fragmentPasswordBinding.tenGuessesStrengthMeter,
                                               fragmentPasswordBinding.tenGuessesStrength)
        
        fragmentPasswordBinding.hundredGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(hundredCrackTimeString)
        resultUtils.setStrengthProgressAndText(resultUtils.crackTimeResult(hundredCrackTimeMilliSeconds),
                                               fragmentPasswordBinding.hundredGuessesStrengthMeter,
                                               fragmentPasswordBinding.hundredGuessesStrength)
        
        // Warning
        val localizedFeedback =
            strength.feedback.withResourceBundle(LocaleUtils.localizedFeedbackResourceBundle(context))
        fragmentPasswordBinding.warningSubtitle.text = resultUtils.getWarningText(localizedFeedback,
                                                                                  tenBCrackTimeResult)
        
        // Suggestions
        fragmentPasswordBinding.suggestionsSubtitle.text = resultUtils.getSuggestionsText(localizedFeedback)
        
        // Guesses
        val guesses = strength.guesses
        fragmentPasswordBinding.guessesSubtitle.text = resultUtils.getGuessesText(guesses)
        
        // Order of magnitude of guesses
        fragmentPasswordBinding.orderMagnSubtitle.text = strength.guessesLog10.formatToTwoDecimalPlaces()
        
        // Entropy
        @SuppressLint("SetTextI18n")
        fragmentPasswordBinding.entropySubtitle.text = "${log2(guesses).formatToTwoDecimalPlaces()} ${context.getString(R.string.bits)}"
        
        // Match sequence
        fragmentPasswordBinding.matchSequenceSubtitle.text = resultUtils.getMatchSequenceText(strength)
        
        // Statistics
        val statsList = resultUtils.getStatisticsCounts(password)
        fragmentPasswordBinding.lengthText.text = statsList[0].toString()
        fragmentPasswordBinding.uppercaseText.text = statsList[1].toString()
        fragmentPasswordBinding.lowercaseText.text = statsList[2].toString()
        fragmentPasswordBinding.numbersText.text = statsList[3].toString()
        fragmentPasswordBinding.specialCharsText.text = statsList[4].toString()
        fragmentPasswordBinding.spacesText.text = statsList[5].toString()
    }
    
}