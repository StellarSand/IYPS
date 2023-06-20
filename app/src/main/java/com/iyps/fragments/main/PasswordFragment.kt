/*
 * Copyright (c) 2022-present the-weird-aquarian
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

package com.iyps.fragments.main

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.utils.IntentUtils.Companion.clearClipboard
import com.iyps.utils.PasswordUtils.Companion.passwordCrackTimeResult
import com.iyps.utils.PasswordUtils.Companion.replaceStrings
import com.nulabinc.zxcvbn.Zxcvbn
import java.util.Locale

class PasswordFragment : Fragment() {
    
    private var _binding: FragmentPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var zxcvbn: Zxcvbn
    private lateinit var suggestionText: StringBuilder
    private var wait = 0
    private var clearClipboardTimer: CountDownTimer? = null
    private lateinit var clipboardManager: ClipboardManager
    private var copiedFromHere = false
    private var worstMeterColor = 0
    private var weakMeterColor = 0
    private var mediumMeterColor = 0
    private var strongMeterColor = 0
    private var excellentMeterColor = 0
    private var emptyMeterColor = 0
    private lateinit var worstString: String
    private lateinit var weakString: String
    private lateinit var mediumString: String
    private lateinit var strongString: String
    private lateinit var excellentString: String
    private lateinit var worstPassWarning: String
    private lateinit var weakPassWarning: String
    private lateinit var mediumPassWarning: String
    private lateinit var notApplicable: String
    private lateinit var zeroString: String
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        zxcvbn = Zxcvbn()
        suggestionText = StringBuilder()
        clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        wait = 0
        emptyMeterColor = resources.getColor(android.R.color.transparent, requireContext().theme)
        worstMeterColor = resources.getColor(R.color.worstMeterColor, requireContext().theme)
        weakMeterColor = resources.getColor(R.color.weakMeterColor, requireContext().theme)
        mediumMeterColor = resources.getColor(R.color.mediumMeterColor, requireContext().theme)
        strongMeterColor = resources.getColor(R.color.strongMeterColor, requireContext().theme)
        excellentMeterColor = resources.getColor(R.color.excellentMeterColor, requireContext().theme)
        worstString = getString(R.string.worst)
        weakString = getString(R.string.weak)
        mediumString = getString(R.string.medium)
        strongString = getString(R.string.strong)
        excellentString = getString(R.string.excellent)
        worstPassWarning = getString(R.string.worst_pass_warning)
        weakPassWarning = getString(R.string.weak_pass_warning)
        mediumPassWarning = getString(R.string.medium_pass_warning)
        notApplicable = getString(R.string.na)
        zeroString = getString(R.string.zero)
        
        /*########################################################################################*/
        
        (requireActivity() as MainActivity).activityBinding.selectButton.isVisible = false
        
        fragmentBinding.passwordText.addTextChangedListener(object : TextWatcher {
            
            var delayTimer: CountDownTimer? = null
            
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                
                // Introduce a subtle delay
                // So passwords are checked after typing is finished
                delayTimer.apply {
                    this?.cancel()
                    
                    object : CountDownTimer(wait.toLong(), 100) {
                        
                        override fun onTick(millisUntilFinished: Long) {}
                        
                        // On timer finish, perform task
                        override fun onFinish() {
                            wait = 400
                            
                            // If edit text is not empty
                            if (charSequence.isNotEmpty()) {
                                
                                val strength = zxcvbn.measure(charSequence)
                                
                                val tenBCrackTimeString = strength.crackTimesDisplay.offlineFastHashing1e10PerSecond
                                val tenKCrackTimeString = strength.crackTimesDisplay.offlineSlowHashing1e4perSecond
                                val tenCrackTimeString = strength.crackTimesDisplay.onlineNoThrottling10perSecond
                                val hundredCrackTimeString  = strength.crackTimesDisplay.onlineThrottling100perHour
                                
                                val tenBCrackTimeMilliSeconds =
                                    (strength.crackTimeSeconds.offlineFastHashing1e10PerSecond * 1000).toLong()
                                val tenKCrackTimeMilliSeconds =
                                    (strength.crackTimeSeconds.offlineSlowHashing1e4perSecond * 1000).toLong()
                                val tenCrackTimeMilliSeconds =
                                    (strength.crackTimeSeconds.onlineNoThrottling10perSecond * 1000).toLong()
                                val hundredCrackTimeMilliSeconds =
                                    (strength.crackTimeSeconds.onlineThrottling100perHour * 1000).toLong()
                                
                                // Estimated time to crack
                                fragmentBinding.tenBGuessesSubtitle.text = replaceStrings(tenBCrackTimeString, this@PasswordFragment)
                                tenBStrengthProgressAndText(passwordCrackTimeResult(tenBCrackTimeMilliSeconds))
                                fragmentBinding.tenKGuessesSubtitle.text = replaceStrings(tenKCrackTimeString, this@PasswordFragment)
                                tenKStrengthProgressAndText(passwordCrackTimeResult(tenKCrackTimeMilliSeconds))
                                fragmentBinding.tenGuessesSubtitle.text = replaceStrings(tenCrackTimeString, this@PasswordFragment)
                                tenStrengthProgressAndText(passwordCrackTimeResult(tenCrackTimeMilliSeconds))
                                fragmentBinding.hundredGuessesSubtitle.text = replaceStrings(hundredCrackTimeString, this@PasswordFragment)
                                hundredStrengthProgressAndText(passwordCrackTimeResult(hundredCrackTimeMilliSeconds))
                                
                                // Warning
                                // If empty, set to custom warning message
                                fragmentBinding.warningSubtitle.text =
                                    strength.feedback.getWarning(Locale.getDefault())
                                        .ifEmpty {
                                            when (passwordCrackTimeResult(tenBCrackTimeMilliSeconds)) {
                                                "WORST" -> worstPassWarning // Worst warning
                                                "WEAK" -> weakPassWarning // Weak warning
                                                "MEDIUM" -> mediumPassWarning // Medium warning
                                                else -> notApplicable // For strong & above
                                            }
                                        }
                                
                                // Suggestions
                                strength.feedback.getSuggestions(Locale.getDefault()).apply {
                                    fragmentBinding.suggestionsSubtitle.text =
                                        if (!this.isNullOrEmpty() && this.size != 0) {
                                            suggestionText.setLength(0)
                                            for (sText in this.indices) {
                                                suggestionText.append("\u2022 ").append(this[sText]).append("\n")
                                            }
                                            suggestionText
                                        }
                                        else {
                                            getString(R.string.na)
                                        }
                                }
                                
                                // Guesses
                                val stringValue = strength.guesses.toString()
                                val formattedString =
                                    if (stringValue.contains("E")) {
                                        val splitString = stringValue.split("E")
                                        "${splitString[0]} x 10^${splitString[1]}"
                                    }
                                    else {
                                        stringValue
                                    }
                                fragmentBinding.guessesSubtitle.text = formattedString
                                
                                // Order of magnitude of guesses
                                fragmentBinding.orderMagnSubtitle.text = strength.guessesLog10.toString()
                                
                                // Statistics
                                val length = charSequence.length
                                val upperCaseCount = charSequence.count { it.isUpperCase() }
                                val lowerCaseCount = charSequence.count { it.isLowerCase() }
                                val numbersCount = charSequence.count { it.isDigit() }
                                val specialCharsCount = length - upperCaseCount - lowerCaseCount - numbersCount
                                fragmentBinding.lengthText.text = length.toString()
                                fragmentBinding.upperCaseText.text = upperCaseCount.toString()
                                fragmentBinding.lowerCaseText.text = lowerCaseCount.toString()
                                fragmentBinding.numbersText.text = numbersCount.toString()
                                fragmentBinding.specialCharsText.text = specialCharsCount.toString()
                                
                            }
                            // If edit text is empty or cleared, reset everything
                            else {
                                detailsReset()
                            }
                        }
                    }.start()
                }
            }
            
            override fun afterTextChanged(editable: Editable) {}
        })
        
        // Clear clipboard after 1 minute if copied from this app
        clipboardManager.addPrimaryClipChangedListener {
            copiedFromHere = true
            
            clearClipboardTimer.apply {
                this?.cancel()
                object : CountDownTimer(60000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    
                    // On timer finish, perform task
                    override fun onFinish() {
                        clearClipboard(clipboardManager)
                        copiedFromHere = false
                    }
                }.start()
            }
        }
    }
    
    private fun strengthProgressAndTextMap(timeToCrackResult: String): Triple<Int, Int, String?> {
        val map = mapOf("WORST" to Triple(20, worstMeterColor, worstString),
                        "WEAK" to Triple(40, weakMeterColor, weakString),
                        "MEDIUM" to Triple(60, mediumMeterColor, mediumString),
                        "STRONG" to Triple(80, strongMeterColor, strongString),
                        "EXCELLENT" to Triple(100, excellentMeterColor, excellentString))
        
        return map[timeToCrackResult] ?: Triple(0, emptyMeterColor, "NA")
    }
    
    private fun tenBStrengthProgressAndText(timeToCrackResult: String) {
        val (progress, indicatorColor, strengthText) = strengthProgressAndTextMap(timeToCrackResult)
        fragmentBinding.tenBGuessesStrengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        fragmentBinding.tenBGuessesStrength.text = strengthText
    }
    
    private fun tenKStrengthProgressAndText(timeToCrackResult: String) {
        val (progress, indicatorColor, strengthText) = strengthProgressAndTextMap(timeToCrackResult)
        fragmentBinding.tenKGuessesStrengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        fragmentBinding.tenKGuessesStrength.text = strengthText
    }
    
    private fun tenStrengthProgressAndText(timeToCrackResult: String) {
        val (progress, indicatorColor, strengthText) = strengthProgressAndTextMap(timeToCrackResult)
        fragmentBinding.tenGuessesStrengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        fragmentBinding.tenGuessesStrength.text = strengthText
    }
    
    private fun hundredStrengthProgressAndText(timeToCrackResult: String) {
        val (progress, indicatorColor, strengthText) = strengthProgressAndTextMap(timeToCrackResult)
        fragmentBinding.hundredGuessesStrengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        fragmentBinding.hundredGuessesStrength.text = strengthText
    }
    
    // Reset details
    private fun detailsReset() {
        fragmentBinding.tenBGuessesStrength.text = notApplicable
        fragmentBinding.tenKGuessesStrength.text = notApplicable
        fragmentBinding.tenGuessesStrength.text = notApplicable
        fragmentBinding.hundredGuessesStrength.text = notApplicable
        fragmentBinding.tenBGuessesSubtitle.text = notApplicable
        fragmentBinding.tenKGuessesSubtitle.text = notApplicable
        fragmentBinding.tenGuessesSubtitle.text = notApplicable
        fragmentBinding.hundredGuessesSubtitle.text = notApplicable
        fragmentBinding.warningSubtitle.text = notApplicable
        fragmentBinding.suggestionsSubtitle.text = notApplicable
        fragmentBinding.guessesSubtitle.text = notApplicable
        fragmentBinding.orderMagnSubtitle.text = notApplicable
        fragmentBinding.lengthText.text = zeroString
        fragmentBinding.upperCaseText.text = zeroString
        fragmentBinding.lowerCaseText.text = zeroString
        fragmentBinding.numbersText.text = zeroString
        fragmentBinding.specialCharsText.text = zeroString
        
        fragmentBinding.tenBGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
        fragmentBinding.tenKGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
        fragmentBinding.tenGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
        fragmentBinding.hundredGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
    }
    
    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        clearClipboard(clipboardManager)
        copiedFromHere = false
        _binding = null
    }
}