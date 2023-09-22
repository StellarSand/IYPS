/*
 * Copyright (c) 2022 StellarSand
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

package com.iyps.fragments.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.utils.FormatUtils.Companion.formatToTwoDecimalPlaces
import com.iyps.utils.UiUtils.Companion.getGuessesText
import com.iyps.utils.UiUtils.Companion.getMatchSequenceText
import com.iyps.utils.UiUtils.Companion.getStatisticsCounts
import com.iyps.utils.UiUtils.Companion.getSuggestionsText
import com.iyps.utils.UiUtils.Companion.getWarningText
import com.iyps.utils.UiUtils.Companion.passwordCrackTimeResult
import com.iyps.utils.UiUtils.Companion.replaceCrackTimeStrings
import com.iyps.utils.UiUtils.Companion.setStrengthProgressAndText
import kotlin.math.log2

class DetailsFragment : Fragment() {
    
    private var _binding: FragmentPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var passwordString: String
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val zxcvbn = (requireContext().applicationContext as ApplicationManager).zxcvbn
        passwordString = (requireActivity() as DetailsActivity).passwordLine
        
        /*########################################################################################*/
        
        fragmentBinding.passwordText.apply {
            setText(passwordString)
            isFocusable = false
            isCursorVisible = false
        }
        
        val strength = zxcvbn.measure(passwordString)
        
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
        fragmentBinding.warningSubtitle.text = getWarningText(requireContext(),
                                                              strength,
                                                              passwordCrackTimeResult(tenBCrackTimeMilliSeconds))
        
        // Suggestions
        fragmentBinding.suggestionsSubtitle.text = getSuggestionsText(requireContext(), strength)
        
        // Guesses
        val guesses = strength.guesses
        fragmentBinding.guessesSubtitle.text = getGuessesText(guesses)
        
        // Order of magnitude of guesses
        fragmentBinding.orderMagnSubtitle.text = strength.guessesLog10.formatToTwoDecimalPlaces()
        
        // Entropy
        @SuppressLint("SetTextI18n")
        fragmentBinding.entropySubtitle.text =
            "${log2(guesses).formatToTwoDecimalPlaces()} ${getString(R.string.bits)}"
        
        // Match sequence
        fragmentBinding.matchSequenceSubtitle.text = getMatchSequenceText(requireContext(), strength)
        
        // Statistics
        val statsList = getStatisticsCounts(passwordString)
        fragmentBinding.lengthText.text = statsList[0].toString()
        fragmentBinding.upperCaseText.text = statsList[1].toString()
        fragmentBinding.lowerCaseText.text = statsList[2].toString()
        fragmentBinding.numbersText.text = statsList[3].toString()
        fragmentBinding.specialCharsText.text = statsList[4].toString()
    }
    
    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}