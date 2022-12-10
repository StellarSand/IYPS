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
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.activities.HelpActivity
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
    private var isExpanded = false
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
        private var baseScore = 0
        private var lengthScore = 0
        private var upperCaseScore = 0
        private var numScore = 0
        private var specialCharScore = 0
        private var penaltyScore = 0
        private var totalScore = 0
        private var lengthCount = 0
        private var upperCaseCount = 0
        private var lowerCaseCount = 0
        private var numCount = 0
        private var specialCharCount = 0
        private var passwordLength = 0
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
        private var zero: String? = null
        private const val lengthScoreMultiplier = 3
        private const val upperCaseScoreMultiplier = 4
        private const val numScoreMultiplier = 5
        private const val specialCharScoreMultiplier = 5
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
        baseScore = 0
        lengthScore = 0
        upperCaseScore = 0
        numScore = 0
        specialCharScore = 0
        penaltyScore = 0
        totalScore = 0
        lengthCount = 0
        upperCaseCount = 0
        numCount = 0
        specialCharCount = 0
        worst = getString(R.string.worst)
        weak = getString(R.string.weak)
        medium = getString(R.string.medium)
        strong = getString(R.string.strong)
        excellent = getString(R.string.excellent)
        worstPassWarning = getString(R.string.worst_pass_warning)
        weakPassWarning = getString(R.string.weak_pass_warning)
        mediumPassWarning = getString(R.string.medium_pass_warning)
        not_applicable = getString(R.string.not_applicable)
        zero = getString(R.string.zero)
        emptyMeterColor = getColor(R.color.hintColor)
        worstMeterColor = getColor(R.color.worstMeterColor)
        weakMeterColor = getColor(R.color.weakMeterColor)
        mediumMeterColor = getColor(R.color.mediumMeterColor)
        strongMeterColor = getColor(R.color.strongMeterColor)
        excellentMeterColor = getColor(R.color.excellentMeterColor)

        /*########################################################################################*/

        fragmentBinding.passwordBox.visibility = View.GONE

        passwordString = (requireActivity() as DetailsActivity).passwordLine
        passwordLength = passwordString!!.length

        val strength = zxcvbn.measure(passwordString)

        val timeToCrackString = strength.crackTimesDisplay.offlineSlowHashing1e4perSecond
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

        // Estimated time to crack
        assert(timeToCrackString != null)
        fragmentBinding.timeToCrackSubtitle.text = replaceStrings(timeToCrackString, this@DetailsFragment)

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

        // Score
        score(passwordString, passwordLength)

        // On click score text view, expand layout
        fragmentBinding.scoreTextView.setOnClickListener {

            if (!isExpanded) {
                fragmentBinding.expandedLayout.visibility = View.VISIBLE
                isExpanded = true
                fragmentBinding.scoreTextView
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_up_arrow,
                                                                    0,
                                                                    0,
                                                                    0)
            }
            else {
                fragmentBinding.expandedLayout.visibility = View.GONE
                isExpanded = false
                fragmentBinding.scoreTextView
                    .setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_down_arrow,
                                                                    0,
                                                                    0,
                                                                    0)
            }
        }

        // On click score help icon
        fragmentBinding.scoreHelp.setOnClickListener {
            startActivity(
                Intent(requireActivity(), HelpActivity::class.java)
                .putExtra("fragment", "Score Help"))
        }


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

    // Password score
    private fun score(passwordString: String?, passwordStringLength: Int) {

        // Scoring system:
        // If password length greater than 5, base score = 10, else 0
        // If password length greater than 5, length score = 3 points for each extra character, else 0
        // Upper case score = 4 points for each upper case char
        // Numbers score = 5 points for each number
        // Special char = 5 points for each special char
        // Penalty = if all upper case/lower case/numbers/special char, subtract 15 points total

        if (passwordStringLength > 5) {

            // Base score
            baseScore = 10
            fragmentBinding.baseScore.text = baseScore.toString()

            for (i in 0 until passwordStringLength) {

                if (Character.isUpperCase(passwordString!![i])) {
                    upperCaseCount++
                }
                else if (Character.isLowerCase(passwordString[i])) {
                    lowerCaseCount++
                }
                else if (Character.isDigit(passwordString[i])) {
                    numCount++
                }
                else if (!Character.isDigit(passwordString[i])
                    && !Character.isLetter(passwordString[i])
                    && !Character.isWhitespace(passwordString[i])) {
                    specialCharCount++
                }
            }

            for (j in 0 until (passwordStringLength - 5)) {
                lengthCount++
            }

            if (upperCaseCount == passwordStringLength) {
                penaltyScore = -15
                fragmentBinding.penaltyTitle.text = getString(R.string.all_upper_penalty)
            }
            else if (lowerCaseCount == passwordStringLength) {
                penaltyScore = -15
                fragmentBinding.penaltyTitle.text = getString(R.string.all_lower_penalty)
            }
            else if (numCount == passwordStringLength) {
                penaltyScore = -15
                fragmentBinding.penaltyTitle.text = getString(R.string.all_num_penalty)
            }
            else if (specialCharCount == passwordStringLength) {
                penaltyScore = -15
                fragmentBinding.penaltyTitle.text = getString(R.string.all_special_char_penalty)
            }
            else {
                penaltyScore = 0
                fragmentBinding.penaltyTitle.text = zero
            }

            lengthScore = lengthScoreMultiplier * lengthCount
            upperCaseScore = upperCaseScoreMultiplier * upperCaseCount
            numScore = numScoreMultiplier * numCount
            specialCharScore = specialCharScoreMultiplier * specialCharCount
            totalScore = baseScore + lengthScore + upperCaseScore + numScore + specialCharScore + penaltyScore
        }
        // If password length less than 5, reset all scores to 0
        else {
            scoreReset()
        }

        // Length score
        fragmentBinding.lengthScore.text = lengthScore.toString()
        lengthCount = 0
        lengthScore = 0

        // Upper case score
        fragmentBinding.upperCaseScore.text = upperCaseScore.toString()
        upperCaseCount = 0
        upperCaseScore = 0

        // Numbers score
        fragmentBinding.numScore.text = numScore.toString()
        numCount = 0
        numScore = 0

        // Special char score
        fragmentBinding.specialCharScore.text = specialCharScore.toString()
        specialCharCount = 0
        specialCharScore = 0

        // Penalty score
        if (penaltyScore != 0) {
            fragmentBinding.penaltyLayout.visibility = View.VISIBLE
            fragmentBinding.penaltyScore.text = penaltyScore.toString()
        }
        else {
            fragmentBinding.penaltyLayout.visibility = View.GONE
        }
        penaltyScore = 0
        lowerCaseCount = 0

        // Total score
        fragmentBinding.totalScore.text = totalScore.toString()
        totalScore = 0
    }

    // Reset score
    private fun scoreReset() {
        fragmentBinding.totalScore.text = zero
        fragmentBinding.lengthScore.text = zero
        fragmentBinding.baseScore.text = zero
        fragmentBinding.upperCaseScore.text = zero
        fragmentBinding.numScore.text = zero
        fragmentBinding.specialCharScore.text = zero
        fragmentBinding.penaltyScore.text = zero
        fragmentBinding.penaltyLayout.visibility = View.GONE
    }

    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        clearClipboard(clipboardManager)
        copiedFromHere = false
        _binding = null
    }
}