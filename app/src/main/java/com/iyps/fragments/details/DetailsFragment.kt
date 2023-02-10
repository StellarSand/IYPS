/*
 * Copyright (c) 2022 the-weird-aquarian
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

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.utils.IntentUtils.Companion.clearClipboard
import com.iyps.utils.PasswordUtils.Companion.passwordCrackTimeResult
import com.iyps.utils.PasswordUtils.Companion.replaceStrings
import com.nulabinc.zxcvbn.Zxcvbn
import java.lang.StringBuilder
import java.util.Locale

class DetailsFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var zxcvbn: Zxcvbn
    private lateinit var suggestionText: StringBuilder
    private var clearClipboardTimer: CountDownTimer? = null
    private lateinit var clipboardManager: ClipboardManager
    private var copiedFromHere = false

    companion object {
        private var worstMeterColor = 0
        private var weakMeterColor = 0
        private var mediumMeterColor = 0
        private var strongMeterColor = 0
        private var excellentMeterColor = 0
        private var emptyMeterColor = 0
        private var worst: String? = null
        private var weak: String? = null
        private var medium: String? = null
        private var strong: String? = null
        private var excellent: String? = null
        private var worstPassWarning: String? = null
        private var weakPassWarning: String? = null
        private var mediumPassWarning: String? = null
        private var not_applicable: String? = null
        private var passwordString: String? = null
    }

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
        worst = getString(R.string.worst)
        weak = getString(R.string.weak)
        medium = getString(R.string.medium)
        strong = getString(R.string.strong)
        excellent = getString(R.string.excellent)
        worstPassWarning = getString(R.string.worst_pass_warning)
        weakPassWarning = getString(R.string.weak_pass_warning)
        mediumPassWarning = getString(R.string.medium_pass_warning)
        not_applicable = getString(R.string.not_applicable)
        emptyMeterColor = getColor(R.color.hintColor)
        worstMeterColor = getColor(R.color.worstMeterColor)
        weakMeterColor = getColor(R.color.weakMeterColor)
        mediumMeterColor = getColor(R.color.mediumMeterColor)
        strongMeterColor = getColor(R.color.strongMeterColor)
        excellentMeterColor = getColor(R.color.excellentMeterColor)

        /*########################################################################################*/

        fragmentBinding.passwordBox.visibility = View.GONE
    
        // Remove 80dp bottom margin in scrollview
        val layoutParams = fragmentBinding.scrollview.layoutParams as (LinearLayout.LayoutParams)
        layoutParams.bottomMargin = 40
        fragmentBinding.scrollview.layoutParams = layoutParams

        passwordString = (requireActivity() as DetailsActivity).passwordLine

        val strength = zxcvbn.measure(passwordString)
    
        val offlineSlowCrackTimeString = strength.crackTimesDisplay.offlineSlowHashing1e4perSecond
        val offlineFastCrackTimeString = strength.crackTimesDisplay.offlineFastHashing1e10PerSecond
        val onlineSlowCrackTimeString  = strength.crackTimesDisplay.onlineThrottling100perHour
        val onlineFastCrackTimeString = strength.crackTimesDisplay.onlineNoThrottling10perSecond
        
        val crackTimeMilliSeconds: Long = (strength.crackTimeSeconds
                                            .offlineSlowHashing1e4perSecond * 1000)
                                            .toLong()


        when (passwordCrackTimeResult(crackTimeMilliSeconds)) {

            "WORST" -> worstStrengthMeter()
            "WEAK" -> weakStrengthMeter()
            "MEDIUM" -> mediumStrengthMeter()
            "STRONG" -> strongStrengthMeter()
            "EXCELLENT" -> excellentStrengthMeter()

        }

        // Basic estimated time to crack
        fragmentBinding.tenKGuessesSubtitle.text = replaceStrings(offlineSlowCrackTimeString, this@DetailsFragment)
        
        // Warning
        // If empty, set to custom warning message
        if (strength.feedback.getWarning(Locale.getDefault()).isEmpty()) {

            when (passwordCrackTimeResult(crackTimeMilliSeconds)) {

                "WORST" -> fragmentBinding.warningSubtitle.text = worstPassWarning // Worst warning
                "WEAK" -> fragmentBinding.warningSubtitle.text = weakPassWarning // Weak warning
                "MEDIUM" -> fragmentBinding.warningSubtitle.text = mediumPassWarning // Medium warning
                else -> fragmentBinding.warningSubtitle.text = not_applicable // For strong & above

            }
        }
        // If not empty, display inbuilt warning
        else {
            fragmentBinding.warningSubtitle.text = strength.feedback.getWarning(
                Locale.getDefault())
        }

        // Suggestions
        val suggestions = strength.feedback.getSuggestions(Locale.getDefault())

        if (suggestions != null && suggestions.size != 0) {
            suggestionText.setLength(0)
            for (sText in suggestions.indices) {
                suggestionText.append("\u2022 ").append(suggestions[sText]).append("\n")
            }
            fragmentBinding.suggestionsSubtitle.text = suggestionText.toString()
        }
        else {
            fragmentBinding.suggestionsSubtitle.text = getString(R.string.not_applicable)
        }
    
        // Advanced estimated time to crack
        fragmentBinding.tenBGuessesSubtitle.text = replaceStrings(offlineFastCrackTimeString, this@DetailsFragment)
        fragmentBinding.tenGuessesSubtitle.text = replaceStrings(onlineFastCrackTimeString, this@DetailsFragment)
        fragmentBinding.hundredGuessesSubtitle.text = replaceStrings(onlineSlowCrackTimeString, this@DetailsFragment)
    
        // Guesses
        fragmentBinding.guessesSubtitle.text = strength.guesses.toString()
    
        // Order of magnitude of guesses
        fragmentBinding.orderMagnSubtitle.text = strength.guessesLog10.toString()


        // Clear clipboard after 1 minute if copied from this app
        clipboardManager.addPrimaryClipChangedListener {

            copiedFromHere = true

            if (clearClipboardTimer != null) {
                clearClipboardTimer!!.cancel()
            }

            clearClipboardTimer = object : CountDownTimer(60000, 1000) {

                override fun onTick(millisUntilFinished: Long) {}

                // On timer finish, perform task
                override fun onFinish() {
                    clearClipboard(clipboardManager)
                    copiedFromHere = false
                }

            }.start()
        }
    }

    private fun getColor(color: Int): Int {
        return resources.getColor(color, requireActivity().theme)
    }

    // Worst strength meter
    private fun worstStrengthMeter() {
        fragmentBinding.strengthSubtitle.text = worst

        fragmentBinding.worstMeter.setIndicatorColor(worstMeterColor)
        fragmentBinding.weakMeter.setIndicatorColor(emptyMeterColor)
        fragmentBinding.mediumMeter.setIndicatorColor(emptyMeterColor)
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor)
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor)

        fragmentBinding.worstMeter.setProgressCompat(100, true)
        fragmentBinding.weakMeter.setProgressCompat(0, true)
        fragmentBinding.mediumMeter.setProgressCompat(0, true)
        fragmentBinding.strongMeter.setProgressCompat(0, true)
        fragmentBinding.excellentMeter.setProgressCompat(0, true)
    }

    // Weak strength meter
    private fun weakStrengthMeter() {
        fragmentBinding.strengthSubtitle.text = weak

        fragmentBinding.worstMeter.setIndicatorColor(weakMeterColor)
        fragmentBinding.weakMeter.setIndicatorColor(weakMeterColor)
        fragmentBinding.mediumMeter.setIndicatorColor(emptyMeterColor)
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor)
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor)

        fragmentBinding.worstMeter.setProgressCompat(100, true)
        fragmentBinding.weakMeter.setProgressCompat(100, true)
        fragmentBinding.mediumMeter.setProgressCompat(0, true)
        fragmentBinding.strongMeter.setProgressCompat(0, true)
        fragmentBinding.excellentMeter.setProgressCompat(0, true)
    }

    // Medium strength meter
    private fun mediumStrengthMeter() {
        fragmentBinding.strengthSubtitle.text = medium

        fragmentBinding.worstMeter.setIndicatorColor(mediumMeterColor)
        fragmentBinding.weakMeter.setIndicatorColor(mediumMeterColor)
        fragmentBinding.mediumMeter.setIndicatorColor(mediumMeterColor)
        fragmentBinding.strongMeter.setIndicatorColor(emptyMeterColor)
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor)

        fragmentBinding.worstMeter.setProgressCompat(100, true)
        fragmentBinding.weakMeter.setProgressCompat(100, true)
        fragmentBinding.mediumMeter.setProgressCompat(100, true)
        fragmentBinding.strongMeter.setProgressCompat(0, true)
        fragmentBinding.excellentMeter.setProgressCompat(0, true)
    }

    // Strong strength meter
    private fun strongStrengthMeter() {
        fragmentBinding.strengthSubtitle.text = strong

        fragmentBinding.worstMeter.setIndicatorColor(strongMeterColor)
        fragmentBinding.weakMeter.setIndicatorColor(strongMeterColor)
        fragmentBinding.mediumMeter.setIndicatorColor(strongMeterColor)
        fragmentBinding.strongMeter.setIndicatorColor(strongMeterColor)
        fragmentBinding.excellentMeter.setIndicatorColor(emptyMeterColor)

        fragmentBinding.worstMeter.setProgressCompat(100, true)
        fragmentBinding.weakMeter.setProgressCompat(100, true)
        fragmentBinding.mediumMeter.setProgressCompat(100, true)
        fragmentBinding.strongMeter.setProgressCompat(100, true)
        fragmentBinding.excellentMeter.setProgressCompat(0, true)
    }

    // Excellent strength meter
    private fun excellentStrengthMeter() {
        fragmentBinding.strengthSubtitle.text = excellent

        fragmentBinding.worstMeter.setIndicatorColor(excellentMeterColor)
        fragmentBinding.weakMeter.setIndicatorColor(excellentMeterColor)
        fragmentBinding.mediumMeter.setIndicatorColor(excellentMeterColor)
        fragmentBinding.strongMeter.setIndicatorColor(excellentMeterColor)
        fragmentBinding.excellentMeter.setIndicatorColor(excellentMeterColor)

        fragmentBinding.worstMeter.setProgressCompat(100, true)
        fragmentBinding.weakMeter.setProgressCompat(100, true)
        fragmentBinding.mediumMeter.setProgressCompat(100, true)
        fragmentBinding.strongMeter.setProgressCompat(100, true)
        fragmentBinding.excellentMeter.setProgressCompat(100, true)
    }

    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        clearClipboard(clipboardManager)
        copiedFromHere = false
        _binding = null
    }
}