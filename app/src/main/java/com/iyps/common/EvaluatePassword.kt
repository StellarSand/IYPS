package com.iyps.common

import android.annotation.SuppressLint
import android.content.Context
import com.iyps.R
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.iyps.utils.LocaleUtils.Companion.localizedFeedbackResourceBundle
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
        fragmentPasswordBinding.tenBGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(tenBCrackTimeString)
        val tenBCrackTimeScore = resultUtils.crackTimeScore(tenBCrackTimeMillis)
        resultUtils.setStrengthProgressAndText(tenBCrackTimeScore,
                                               fragmentPasswordBinding.tenBGuessesStrengthMeter,
                                               fragmentPasswordBinding.tenBGuessesStrength)
        
        fragmentPasswordBinding.tenKGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(tenKCrackTimeString)
        resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(tenKCrackTimeMillis),
                                               fragmentPasswordBinding.tenKGuessesStrengthMeter,
                                               fragmentPasswordBinding.tenKGuessesStrength)
        
        fragmentPasswordBinding.tenGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(tenCrackTimeString)
        resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(tenCrackTimeMillis),
                                               fragmentPasswordBinding.tenGuessesStrengthMeter,
                                               fragmentPasswordBinding.tenGuessesStrength)
        
        fragmentPasswordBinding.hundredGuessesSubtitle.text =
            resultUtils.replaceCrackTimeStrings(hundredCrackTimeString)
        resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(hundredCrackTimeMillis),
                                               fragmentPasswordBinding.hundredGuessesStrengthMeter,
                                               fragmentPasswordBinding.hundredGuessesStrength)
        
        // Warning
        val localizedFeedback =
            strength.feedback.withResourceBundle(localizedFeedbackResourceBundle(context))
        fragmentPasswordBinding.warningSubtitle.text = resultUtils.getWarningText(localizedFeedback,
                                                                                  tenBCrackTimeScore)
        
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