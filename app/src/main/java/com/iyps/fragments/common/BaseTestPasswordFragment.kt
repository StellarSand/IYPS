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

package com.iyps.fragments.common

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.databinding.FragmentTestPasswordBinding
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.FormatUtils.Companion.generateNewFilename
import com.iyps.utils.IntentUtils.Companion.shareText
import com.iyps.utils.LocaleUtils.Companion.localizedFeedbackResourceBundle
import com.iyps.utils.ResultUtils
import com.iyps.utils.UiUtils.Companion.showSnackbar
import com.nulabinc.zxcvbn.Zxcvbn
import org.koin.android.ext.android.get
import java.text.NumberFormat

abstract class BaseTestPasswordFragment : Fragment() {
    
    private var _binding: FragmentTestPasswordBinding? = null
    protected val fragmentBinding get() = _binding!!
    private val resultUtils by lazy { ResultUtils(requireContext()) }
    protected lateinit var clipboardManager: ClipboardManager
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        setupFragmentContent()
        
        // Copy
        fragmentBinding.copyChip.setOnClickListener {
            copyToClipboard(getFormattedResultsText())
        }
        
        // Share
        fragmentBinding.shareChip.setOnClickListener {
            requireActivity().shareText(getFormattedResultsText())
        }
        
        // Export
        fragmentBinding.exportChip.setOnClickListener {
            exportToFilePicker.launch(generateNewFilename())
        }
    }
    
    // Subclasses must override
    protected abstract fun setupFragmentContent()
    
    protected fun copyToClipboard(copiedText: CharSequence) {
        val clipData = ClipData.newPlainText("IYPS", copiedText)
        clipData.hideSensitiveContent()
        clipboardManager.setPrimaryClip(clipData)
        
        if (Build.VERSION.SDK_INT <= 32) {
            showSnackbar(
                getCoordinatorLayout(),
                requireContext().getString(R.string.copied_to_clipboard),
                getSnackbarAnchorView()
            )
        }
    }
    
    @SuppressLint("SetTextI18n")
    protected fun displayResults(password: CharSequence) {
        val strength = get<Zxcvbn>().measure(password)
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
        
        fragmentBinding.apply {
            
            // Estimated time to crack
            tenBGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(tenBCrackTimeString)
            val tenBCrackTimeScore = resultUtils.crackTimeScore(tenBCrackTimeMillis)
            resultUtils.setStrengthProgressAndText(tenBCrackTimeScore,
                                                   tenBGuessesStrengthMeter,
                                                   tenBGuessesStrength)
            
            tenKGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(tenKCrackTimeString)
            resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(tenKCrackTimeMillis),
                                                   tenKGuessesStrengthMeter,
                                                   tenKGuessesStrength)
            
            tenGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(tenCrackTimeString)
            resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(tenCrackTimeMillis),
                                                   tenGuessesStrengthMeter,
                                                   tenGuessesStrength)
            
            hundredGuessesSubtitle.text = resultUtils.replaceCrackTimeStrings(hundredCrackTimeString)
            resultUtils.setStrengthProgressAndText(resultUtils.crackTimeScore(hundredCrackTimeMillis),
                                                   hundredGuessesStrengthMeter,
                                                   hundredGuessesStrength)
            
            // Warning
            val localizedFeedback =
                strength.feedback.withResourceBundle(localizedFeedbackResourceBundle(requireContext()))
            warningSubtitle.text = resultUtils.getWarningText(localizedFeedback, tenBCrackTimeScore)
            
            // Suggestions
            suggestionsSubtitle.text = resultUtils.getSuggestionsText(localizedFeedback)
            
            // Guesses
            val guesses = strength.guesses
            guessesSubtitle.text = resultUtils.getGuessesText(guesses)
            
            // Order of magnitude of guesses
            orderMagnSubtitle.text = NumberFormat.getInstance().format(strength.guessesLog10)
            
            // Entropy
            val statsList = resultUtils.getStatisticsCounts(password)
            entropySubtitle.text =
                "${resultUtils.getEntropyText(statsList)} ${getString(R.string.bits)}"
            
            // Match sequence
            matchSequenceSubtitle.text = resultUtils.getMatchSequenceText(strength)
            
            // Statistics
            statsSubtitle.text =
                buildString {
                    append(
                        "\u2022 ${getString(R.string.length)}: ${NumberFormat.getInstance().format(statsList[0])}",
                        "\n\u2022 ${getString(R.string.uppercase)}: ${NumberFormat.getInstance().format(statsList[1])}",
                        "\n\u2022 ${getString(R.string.lowercase)}: ${NumberFormat.getInstance().format(statsList[2])}",
                        "\n\u2022 ${getString(R.string.numbers)}: ${NumberFormat.getInstance().format(statsList[3])}",
                        "\n\u2022 ${getString(R.string.special_char)}: ${NumberFormat.getInstance().format(statsList[4])}",
                        "\n\u2022 ${getString(R.string.spaces)}: ${NumberFormat.getInstance().format(statsList[5])}"
                    )
                }
        }
    }
    
    private fun getFormattedResultsText(): String {
        return buildString {
            append("# ${getString(R.string.password)}\n")
            append("${fragmentBinding.passwordText.text.toString()}\n\n")
            append("## ${getString(R.string.est_time_to_crack)}\n\n")
            append("#### ${getString(R.string.ten_b_guesses_per_sec)}\n")
            append("\u2022 ${fragmentBinding.tenBGuessesSubtitle.text}\n")
            append("\u2022 ${getString(R.string.strength)}: ${fragmentBinding.tenBGuessesStrength.text}\n\n")
            append("#### ${getString(R.string.ten_k_guesses_per_sec)}\n")
            append("\u2022 ${fragmentBinding.tenKGuessesSubtitle.text}\n")
            append("\u2022 ${getString(R.string.strength)}: ${fragmentBinding.tenKGuessesStrength.text}\n\n")
            append("#### ${getString(R.string.ten_guesses_per_sec)}\n")
            append("\u2022 ${fragmentBinding.tenGuessesSubtitle.text}\n")
            append("\u2022 ${getString(R.string.strength)}: ${fragmentBinding.tenGuessesStrength.text}\n\n")
            append("#### ${getString(R.string.hundred_guesses_per_hour)}\n")
            append("\u2022 ${fragmentBinding.hundredGuessesSubtitle.text}\n")
            append("\u2022 ${getString(R.string.strength)}: ${fragmentBinding.hundredGuessesStrength.text}\n\n")
            append("## ${getString(R.string.warning)}\n")
            append("${fragmentBinding.warningSubtitle.text}\n\n")
            append("## ${getString(R.string.suggestions)}\n")
            append("${fragmentBinding.suggestionsSubtitle.text}\n\n")
            append("## ${getString(R.string.est_guesses_to_crack)}\n")
            append("${fragmentBinding.guessesSubtitle.text.replace(
                // Insert ^ after "x 10" (before superscript)
                Regex("\u00D7\\s+10(\\d+)"), "\u00D7 10\u005E$1"
            )}\n\n")
            append("## ${getString(R.string.order_of_magn)}\n")
            append("${fragmentBinding.orderMagnSubtitle.text}\n\n")
            append("## ${getString(R.string.entropy)}\n")
            append("${fragmentBinding.entropySubtitle.text}\n\n")
            append("## ${getString(R.string.match_sequence)}\n")
            append("${fragmentBinding.matchSequenceSubtitle.text}\n\n")
            append("## ${getString(R.string.statistics)}\n")
            append("${fragmentBinding.statsSubtitle.text}")
        }
    }
    
    protected val exportToFilePicker =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            uri?.let {
                try {
                    requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(getFormattedResultsText().toByteArray())
                    }
                    showSnackbar(
                        getCoordinatorLayout(),
                        getString(R.string.export_success),
                        getSnackbarAnchorView()
                    )
                }
                catch (_: Exception) {
                    showSnackbar(
                        getCoordinatorLayout(),
                        getString(R.string.export_fail),
                        getSnackbarAnchorView()
                    )
                }
            }
        }
    
    // Subclasses must override
    protected abstract fun getCoordinatorLayout(): CoordinatorLayout
    protected abstract fun getSnackbarAnchorView(): View
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
