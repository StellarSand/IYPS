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

package com.iyps.fragments.main

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.iyps.utils.ClipboardUtils.Companion.clearClipboard
import com.iyps.utils.ClipboardUtils.Companion.manageClipboard
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.iyps.utils.UiUtils.Companion.passwordCrackTimeResult
import com.iyps.utils.UiUtils.Companion.replaceCrackTimeStrings
import com.iyps.utils.UiUtils.Companion.getGuessesText
import com.iyps.utils.UiUtils.Companion.getMatchSequenceText
import com.iyps.utils.UiUtils.Companion.setStrengthProgressAndText
import com.iyps.utils.UiUtils.Companion.getStatisticsCounts
import com.iyps.utils.UiUtils.Companion.getSuggestionsText
import com.iyps.utils.UiUtils.Companion.getWarningText
import com.iyps.utils.UiUtils.Companion.localizedFeedbackResourceBundle
import kotlin.math.log2

class PasswordFragment : Fragment() {
    
    private var _binding: FragmentPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var clipboardManager: ClipboardManager
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val zxcvbn = (requireContext().applicationContext as ApplicationManager).zxcvbn
        clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        /*########################################################################################*/
    
        fragmentBinding.lengthSubtitle.text = "\u2022 ${getString(R.string.length)}"
        fragmentBinding.uppercaseSubtitle.text = "\u2022 ${getString(R.string.uppercase)}"
        fragmentBinding.lowercaseSubtitle.text = "\u2022 ${getString(R.string.lowercase)}"
        fragmentBinding.numbersSubtitle.text = "\u2022 ${getString(R.string.numbers)}"
        fragmentBinding.specialCharsSubtitle.text = "\u2022 ${getString(R.string.special_char)}"
        
        fragmentBinding.passwordText.apply {
            
            if (PreferenceManager(requireContext()).getBoolean(INCOG_KEYBOARD)) {
                imeOptions = IME_FLAG_NO_PERSONALIZED_LEARNING
                inputType = TYPE_TEXT_VARIATION_PASSWORD
            }
            
            addTextChangedListener(object : TextWatcher {
                
                var delayTimer: CountDownTimer? = null
                
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    
                    // Introduce a subtle delay
                    // So passwords are checked after typing is finished
                    delayTimer?.cancel()
                    delayTimer = object : CountDownTimer(400, 100) {
                        
                        override fun onTick(millisUntilFinished: Long) {}
                        
                        // On timer finish, perform task
                        override fun onFinish() {
                            
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
                                fragmentBinding.tenBGuessesSubtitle.text = replaceCrackTimeStrings(tenBCrackTimeString, requireContext())
                                setStrengthProgressAndText(requireContext(),
                                                           passwordCrackTimeResult(tenBCrackTimeMilliSeconds),
                                                           fragmentBinding.tenBGuessesStrengthMeter,
                                                           fragmentBinding.tenBGuessesStrength)
                                fragmentBinding.tenKGuessesSubtitle.text = replaceCrackTimeStrings(tenKCrackTimeString, requireContext())
                                setStrengthProgressAndText(requireContext(),
                                                           passwordCrackTimeResult(tenKCrackTimeMilliSeconds),
                                                           fragmentBinding.tenKGuessesStrengthMeter,
                                                           fragmentBinding.tenKGuessesStrength)
                                fragmentBinding.tenGuessesSubtitle.text = replaceCrackTimeStrings(tenCrackTimeString, requireContext())
                                setStrengthProgressAndText(requireContext(),
                                                           passwordCrackTimeResult(tenCrackTimeMilliSeconds),
                                                           fragmentBinding.tenGuessesStrengthMeter,
                                                           fragmentBinding.tenGuessesStrength)
                                fragmentBinding.hundredGuessesSubtitle.text = replaceCrackTimeStrings(hundredCrackTimeString, requireContext())
                                setStrengthProgressAndText(requireContext(),
                                                           passwordCrackTimeResult(hundredCrackTimeMilliSeconds),
                                                           fragmentBinding.hundredGuessesStrengthMeter,
                                                           fragmentBinding.hundredGuessesStrength)
                                
                                // Warning
                                val localizedFeedback =
                                    strength.feedback.withResourceBundle(localizedFeedbackResourceBundle(requireContext()))
                                fragmentBinding.warningSubtitle.text = getWarningText(requireContext(),
                                                                                      localizedFeedback,
                                                                                      passwordCrackTimeResult(tenBCrackTimeMilliSeconds))
                                
                                // Suggestions
                                fragmentBinding.suggestionsSubtitle.text = getSuggestionsText(requireContext(), localizedFeedback)
                                
                                // Guesses
                                val guesses = strength.guesses
                                fragmentBinding.guessesSubtitle.text = getGuessesText(guesses)
                                
                                // Order of magnitude of guesses
                                fragmentBinding.orderMagnSubtitle.text = strength.guessesLog10.formatToTwoDecimalPlaces()
                                
                                // Entropy
                                fragmentBinding.entropySubtitle.text =
                                    "${log2(guesses).formatToTwoDecimalPlaces()} ${getString(R.string.bits)}"
                                
                                // Match sequence
                                fragmentBinding.matchSequenceSubtitle.text =
                                    getMatchSequenceText(requireContext(), strength)
                                
                                // Statistics
                                val statsList = getStatisticsCounts(charSequence)
                                fragmentBinding.lengthText.text = statsList[0].toString()
                                fragmentBinding.uppercaseText.text = statsList[1].toString()
                                fragmentBinding.lowercaseText.text = statsList[2].toString()
                                fragmentBinding.numbersText.text = statsList[3].toString()
                                fragmentBinding.specialCharsText.text = statsList[4].toString()
                                
                            }
                            // If edit text is empty or cleared, reset everything
                            else {
                                detailsReset()
                            }
                        }
                    }.start()
                }
                
                override fun afterTextChanged(editable: Editable) {}
            })
        }
        
        // Clipboard
        manageClipboard(clipboardManager)
    }
    
    // Reset details
    private fun detailsReset() {
        val notApplicableString = getString(R.string.na)
        val zeroString = getString(R.string.zero)
        val emptyMeterColor = resources.getColor(android.R.color.transparent, requireContext().theme)
        
        fragmentBinding.tenBGuessesStrength.text = notApplicableString
        fragmentBinding.tenKGuessesStrength.text = notApplicableString
        fragmentBinding.tenGuessesStrength.text = notApplicableString
        fragmentBinding.hundredGuessesStrength.text = notApplicableString
        fragmentBinding.tenBGuessesSubtitle.text = notApplicableString
        fragmentBinding.tenKGuessesSubtitle.text = notApplicableString
        fragmentBinding.tenGuessesSubtitle.text = notApplicableString
        fragmentBinding.hundredGuessesSubtitle.text = notApplicableString
        fragmentBinding.warningSubtitle.text = notApplicableString
        fragmentBinding.suggestionsSubtitle.text = notApplicableString
        fragmentBinding.guessesSubtitle.text = notApplicableString
        fragmentBinding.orderMagnSubtitle.text = notApplicableString
        fragmentBinding.orderMagnSubtitle.text = notApplicableString
        fragmentBinding.entropySubtitle.text = notApplicableString
        fragmentBinding.matchSequenceSubtitle.text = notApplicableString
        fragmentBinding.lengthText.text = zeroString
        fragmentBinding.uppercaseText.text = zeroString
        fragmentBinding.lowercaseText.text = zeroString
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
        _binding = null
    }
}