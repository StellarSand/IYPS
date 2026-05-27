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
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.iyps.R
import com.iyps.databinding.FragmentTestPasswordBinding
import com.iyps.models.GenPhraseDetails
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.ClipboardUtils.Companion.scheduleClipboardClear
import com.iyps.utils.IntentUtils.Companion.shareText
import com.iyps.utils.UiUtils.Companion.showSnackbar
import com.nulabinc.zxcvbn.Feedback
import com.nulabinc.zxcvbn.Pattern
import com.nulabinc.zxcvbn.Strength
import com.nulabinc.zxcvbn.Zxcvbn
import org.koin.android.ext.android.get
import java.io.InputStreamReader
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Enumeration
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.text.ifEmpty

abstract class BaseTestPasswordFragment : Fragment() {
    
    private var _binding: FragmentTestPasswordBinding? = null
    protected val fragmentBinding get() = _binding!!
    
    private val dayStringResource by lazy { getString(R.string.day) }
    private val monthStringResource by lazy { getString(R.string.month) }
    private val yearStringResource by lazy { getString(R.string.year) }
    
    private val timeUnitsReplacementMap by lazy {
        mapOf(
            "second" to Pair(getString(R.string.second), getString(R.string.seconds)),
            "minute" to Pair(getString(R.string.minute), getString(R.string.minutes)),
            "hour" to Pair(getString(R.string.hour), getString(R.string.hours)),
            "day" to Pair(dayStringResource, getString(R.string.days)),
            "month" to Pair(monthStringResource, getString(R.string.months)),
            "year" to Pair(yearStringResource, getString(R.string.years))
        )
    }
    
    private val timeStringsReplacementMap by lazy {
        mapOf(
            "less than a second" to getString(R.string.less_than_sec),
            "centuries" to getString(R.string.centuries)
        )
    }
    
    private val timeStringsRegex = "(\\d+)\\s+(\\w+)".toRegex() // Insert regex meme here
    
    // Take days in:
    // Month = 31, Year = 365
    private val fiveHrsInMillis = TimeUnit.HOURS.toMillis(5)
    private val tenDaysInMillis = TimeUnit.DAYS.toMillis(10)
    private val oneMonthInMillis = TimeUnit.DAYS.toMillis(31)
    private val sixMonthsInMillis = TimeUnit.DAYS.toMillis(186)
    
    protected val emptyMeterColor by lazy { resources.getColor(android.R.color.transparent, requireContext().theme) }
    private val worstMeterColor by lazy { resources.getColor(R.color.color_worstMeter, requireContext().theme) }
    private val weakMeterColor by lazy { resources.getColor(R.color.color_weakMeter, requireContext().theme) }
    private val mediumMeterColor by lazy { resources.getColor(R.color.color_mediumMeter, requireContext().theme) }
    private val strongMeterColor by lazy { resources.getColor(R.color.color_strongMeter, requireContext().theme) }
    private val excellentMeterColor by lazy { resources.getColor(R.color.color_excellentMeter, requireContext().theme) }
    
    private val worstString by lazy { getString(R.string.worst) }
    private val weakString by lazy { getString(R.string.weak) }
    private val mediumString by lazy { getString(R.string.medium) }
    private val strongString by lazy { getString(R.string.strong) }
    private val excellentString by lazy { getString(R.string.excellent) }
    protected val naString by lazy { getString(R.string.na) }
    
    private val worstPassWarning by lazy { getString(R.string.worst_pass_warning) }
    private val weakPassWarning by lazy { getString(R.string.weak_pass_warning) }
    private val mediumPassWarning by lazy { getString(R.string.medium_pass_warning) }
    
    private val patternString by lazy { getString(R.string.pattern) }
    private val orderMagnString by lazy { getString(R.string.order_of_magn) }
    private val dictNameString by lazy { getString(R.string.dict_name) }
    private val rankString by lazy { getString(R.string.rank) }
    private val reversedString by lazy { getString(R.string.reversed) }
    private val substitutionsString by lazy { getString(R.string.substitutions) }
    private val baseTokenString by lazy { getString(R.string.base_token) }
    private val seqNameString by lazy { getString(R.string.sequence_name) }
    private val seqSizeString by lazy { getString(R.string.sequence_size) }
    private val ascendingString by lazy { getString(R.string.ascending) }
    private val dayString by lazy { "\u2022 ${dayStringResource.replaceFirstChar { it.titlecase(Locale.getDefault()) }}" }
    private val monthString by lazy { "\u2022 ${monthStringResource.replaceFirstChar { it.titlecase(Locale.getDefault()) }}" }
    private val yearString by lazy { "\u2022 ${yearStringResource.replaceFirstChar { it.titlecase(Locale.getDefault()) }}" }
    private val separatorString by lazy { getString(R.string.separator) }
    private val graphString by lazy { getString(R.string.graph) }
    private val turnsString by lazy { getString(R.string.turns) }
    private val regexNameString by lazy { getString(R.string.regex_name) }
    private val numberFmt by lazy { NumberFormat.getInstance() }
    protected var isPassphrase = false
    protected lateinit var clipboardManager: ClipboardManager
    
    private companion object {
        
        private const val WORST_SCORE = 1
        private const val WEAK_SCORE = 2
        private const val MEDIUM_SCORE = 3
        private const val STRONG_SCORE = 4
        private const val EXCELLENT_SCORE = 5
        
        private fun localizedFeedbackResourceBundle(context: Context): ResourceBundle {
            val locale = Locale.getDefault().language
            
            // If locale is in zxcvbn4j, use default resource bundle
            // else use custom messages.properties from res/raw
            return if (locale !in setOf("cs", "et", "fa", "pt-rBR", "sv", "tr", "zh-rCN")) {
                ResourceBundle.getBundle("com/nulabinc/zxcvbn/messages")
            }
            else {
                val properties =
                    when(locale) {
                        "cs" -> loadTranslations(context, R.raw.messages_cs)
                        "et" -> loadTranslations(context, R.raw.messages_et)
                        "fa" -> loadTranslations(context, R.raw.messages_fa)
                        "pt-rBR" -> loadTranslations(context, R.raw.messages_pt_br)
                        "sv" -> loadTranslations(context, R.raw.messages_sv)
                        "tr" -> loadTranslations(context, R.raw.messages_tr)
                        else -> loadTranslations(context, R.raw.messages_zh_cn)
                    }
                
                object : ResourceBundle() {
                    override fun handleGetObject(key: String?): Any? {
                        return properties.getProperty(key)
                    }
                    
                    override fun getKeys(): Enumeration<String> {
                        return Collections.enumeration(properties.keys.map { it.toString() })
                    }
                }
            }
        }
        
        private fun loadTranslations(context: Context, resId: Int): Properties {
            return Properties().apply {
                load(InputStreamReader(context.resources.openRawResource(resId), Charsets.UTF_8))
            }
        }
        
    }
    
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
            scheduleClipboardClear(requireContext())
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
    
    // Replace hardcoded strings from the library for proper language support
    private fun replaceCrackTimeStrings(timeToCrackString: String): String {
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
    private fun crackTimeScore(crackTimeMilliSeconds: Long): Int {
        return when (crackTimeMilliSeconds) {
            in 0..fiveHrsInMillis -> WORST_SCORE
            in (fiveHrsInMillis + 1)..tenDaysInMillis -> WEAK_SCORE
            in (tenDaysInMillis + 1)..oneMonthInMillis -> MEDIUM_SCORE
            in (oneMonthInMillis + 1)..sixMonthsInMillis -> STRONG_SCORE
            else -> EXCELLENT_SCORE
        }
    }
    
    private fun setStrengthProgressAndText(crackTimeScore: Int,
                                           strengthMeter: LinearProgressIndicator,
                                           strengthTextView: MaterialTextView) {
        val strengthProgTextMap =
            mapOf(
                WORST_SCORE to Triple(20, worstMeterColor, worstString),
                WEAK_SCORE to Triple(40, weakMeterColor, weakString),
                MEDIUM_SCORE to Triple(60, mediumMeterColor, mediumString),
                STRONG_SCORE to Triple(80, strongMeterColor, strongString),
                EXCELLENT_SCORE to Triple(100, excellentMeterColor, excellentString)
            )
        
        val (progress, indicatorColor, strengthText) =
            strengthProgTextMap[crackTimeScore] ?: Triple(0, emptyMeterColor, naString)
        
        strengthMeter.apply {
            setIndicatorColor(indicatorColor)
            setProgressCompat(progress, true)
        }
        strengthTextView.text = strengthText
    }
    
    private fun getWarningText(localizedFeedback: Feedback,
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
    
    private fun getSuggestionsText(localizedFeedback: Feedback): CharSequence {
        return buildString {
            localizedFeedback.suggestions
                .joinTo(this, "\n") { "\u2022 $it" }
                .ifEmpty { append(naString) }
        }
    }
    
    private fun getGuessesText(guesses: Double): CharSequence {
        return if (guesses >= 1000000) {
            val exponent = floor(log10(guesses)).toInt()
            val mantissa = guesses / 10.0.pow(exponent.toDouble())
            val formattedMantissa = numberFmt.format(mantissa)
            HtmlCompat.fromHtml(
                "$formattedMantissa \u00D7 10<sup><small>$exponent</small></sup>",
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }
        else numberFmt.format(guesses)
    }
    
    private fun getMatchSequenceText(strength: Strength): CharSequence {
        val matchSequence = strength.sequence
        
        val matchesText = buildString {
            matchSequence.forEachIndexed { index, match ->
                val pattern = match.pattern
                
                append("<b>${index + 1}) \"${match.token}\"</b>")
                append("<br>${patternString}: $pattern")
                if (matchSequence.size > 1)
                    append("<br>\u2022 ${orderMagnString}: ${numberFmt.format(match.guessesLog10)}")
                
                when (pattern) {
                    Pattern.Dictionary -> {
                        val dictionaryName =
                            when (match.dictionaryName) {
                                "english_wikipedia" -> "wikipedia"
                                "female_names" -> "names"
                                else -> match.dictionaryName
                            }
                        append("<br>${dictNameString}: $dictionaryName")
                        if (matchSequence.size > 1) append("<br>${rankString}: ${match.rank}")
                        append("<br>${reversedString}: ${match.reversed}")
                        if (match.l33t)
                            append("<br>${substitutionsString}: ${match.subDisplay.removeSurrounding("[", "]")}")
                    }
                    
                    Pattern.Repeat -> {
                        append("<br>${baseTokenString}: ${match.baseToken}")
                        append("<br>${getString(R.string.repeat_times, match.repeatCount.toString())}")
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
                        append("<br>${getString(R.string.shifted_times, match.shiftedCount.toString())}")
                    }
                    
                    Pattern.Regex -> append("<br>${regexNameString}: ${match.regexName}")
                    
                    else -> {}
                }
                
                if (index != matchSequence.lastIndex) append("<br>__________________________<br><br>")
            }
        }
        
        return HtmlCompat.fromHtml(matchesText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
    
    private fun getStatisticsCounts(charSequence: CharSequence): Array<Int> {
        val length = charSequence.length
        val uniqueCharsCount = charSequence.toSet().size
        val upperCaseCount = charSequence.count { it.isUpperCase() }
        val lowerCaseCount = charSequence.count { it.isLowerCase() }
        val numbersCount = charSequence.count { it.isDigit() }
        val spacesCount = charSequence.count { it.isWhitespace() }
        val specialCharsCount = length - upperCaseCount - lowerCaseCount - numbersCount - spacesCount
        return arrayOf(
            length,
            uniqueCharsCount,
            upperCaseCount,
            lowerCaseCount,
            numbersCount,
            specialCharsCount,
            spacesCount
        )
    }
    
    private fun getPwdEntropyText(statsCountsList: Array<Int>): String {
        var poolSize = 0.0
        
        // Pool size
        // A-Z -> 26
        // a-z -> 26
        // 0-9 -> 10
        // Special characters -> 32 on a standard US keyboard
        if (statsCountsList[2] > 0) poolSize += 26.0 // Uppercase
        if (statsCountsList[3] > 0) poolSize += 26.0 // Lowercase
        if (statsCountsList[4] > 0) poolSize += 10.0 // Digits
        if (statsCountsList[5] > 0) poolSize += 32.0 // Special characters
        
        return numberFmt.format(
            // Entropy = Length * log2(pool size)
            (statsCountsList[0] * log2(poolSize))
        )
    }
    
    private fun getPhraseEntropyText(wordsInPhrase: Double,
                                     totalWordsInWordlist: Double,
                                     hasNumber: Boolean): Pair<String, String> {
        // Entropy = words_in_passphrase * log2(total_words_in_wordlist)
        // if numbers included, add log2(10.0)
        var entropy = wordsInPhrase * log2(totalWordsInWordlist)
        if (hasNumber) entropy += log2(10.0)
        
        return Pair(
            numberFmt.format(entropy), // Total entropy
            numberFmt.format(entropy / wordsInPhrase) // Per word entropy
        )
    }
    
    @SuppressLint("SetTextI18n")
    protected fun displayPwdResults(password: CharSequence) {
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
            tenBGuessesSubtitle.text = replaceCrackTimeStrings(tenBCrackTimeString)
            val tenBCrackTimeScore = crackTimeScore(tenBCrackTimeMillis)
            setStrengthProgressAndText(tenBCrackTimeScore,
                                       tenBGuessesStrengthMeter,
                                       tenBGuessesStrength)
            
            tenKGuessesSubtitle.text = replaceCrackTimeStrings(tenKCrackTimeString)
            setStrengthProgressAndText(crackTimeScore(tenKCrackTimeMillis),
                                       tenKGuessesStrengthMeter,
                                       tenKGuessesStrength)
            
            tenGuessesSubtitle.text = replaceCrackTimeStrings(tenCrackTimeString)
            setStrengthProgressAndText(crackTimeScore(tenCrackTimeMillis),
                                       tenGuessesStrengthMeter,
                                       tenGuessesStrength)
            
            hundredGuessesSubtitle.text = replaceCrackTimeStrings(hundredCrackTimeString)
            setStrengthProgressAndText(crackTimeScore(hundredCrackTimeMillis),
                                       hundredGuessesStrengthMeter,
                                       hundredGuessesStrength)
            
            // Warning
            val localizedFeedback =
                strength.feedback.withResourceBundle(localizedFeedbackResourceBundle(requireContext()))
            warningSubtitle.text = getWarningText(localizedFeedback, tenBCrackTimeScore)
            
            // Suggestions
            suggestionsSubtitle.text = getSuggestionsText(localizedFeedback)
            
            // Guesses
            val guesses = strength.guesses
            guessesSubtitle.text = getGuessesText(guesses)
            
            // Order of magnitude of guesses
            orderMagnSubtitle.text = numberFmt.format(strength.guessesLog10)
            
            // Entropy
            val statsList = getStatisticsCounts(password)
            entropySubtitle.text = "${getPwdEntropyText(statsList)} ${getString(R.string.bits)}"
            
            // Match sequence
            matchSequenceSubtitle.text = getMatchSequenceText(strength)
            
            // Statistics
            statsSubtitle.text =
                buildString {
                    append(
                        "\u2022 ${getString(R.string.length)}: ${numberFmt.format(statsList[0])}",
                        "\n\u2022 ${getString(R.string.unique_chars)}: ${numberFmt.format(statsList[1])}",
                        "\n\u2022 ${getString(R.string.uppercase)}: ${numberFmt.format(statsList[2])}",
                        "\n\u2022 ${getString(R.string.lowercase)}: ${numberFmt.format(statsList[3])}",
                        "\n\u2022 ${getString(R.string.numbers)}: ${numberFmt.format(statsList[4])}",
                        "\n\u2022 ${getString(R.string.special_char)}: ${numberFmt.format(statsList[5])}",
                        "\n\u2022 ${getString(R.string.spaces)}: ${numberFmt.format(statsList[6])}"
                    )
                }
        }
    }
    
    protected fun displayPhraseResults(passphrase: CharSequence) {
        val phraseDetails =
            if (Build.VERSION.SDK_INT >= 33) arguments?.getParcelable("phraseDetails", GenPhraseDetails::class.java)!!
            else arguments?.getParcelable("phraseDetails")!!
        
        val (totalEntropy, perWordEntropy) =
            getPhraseEntropyText(
                phraseDetails.wordsInPhrase.toDouble(),
                phraseDetails.totalWordsInWordlist.toDouble(),
                phraseDetails.hasNumber
            )
        
        @SuppressLint("SetTextI18n")
        fragmentBinding.entropySubtitle.text =
            buildString {
                append(
                    "\u2022 ${getString(R.string.total)}: $totalEntropy ${getString(R.string.bits)}",
                    "\n\u2022 ${getString(R.string.per_word)}: $perWordEntropy ${getString(R.string.bits)}"
                )
            }
        
        val wordsList =
            passphrase
                .split(phraseDetails.separator)
                .map {
                    it.dropLastWhile { char ->
                        char.isDigit()
                    }
                }
        val longestWord = wordsList.maxBy { it.length }
        val shortestWord = wordsList.minBy { it.length }
        
        // Statistics
        fragmentBinding.statsSubtitle.text =
            buildString {
                append(
                    "\u2022 ${getString(R.string.words)}: ${numberFmt.format(phraseDetails.wordsInPhrase.toInt())}",
                    "\n\u2022 ${getString(R.string.avg_word_length)}: ${
                        numberFmt.format(
                            (longestWord.length + shortestWord.length) / 2
                        )
                    }",
                    "\n\u2022 ${getString(R.string.longest_word)}: $longestWord",
                    "\n\u2022 ${getString(R.string.longest_word_length)}: ${numberFmt.format(longestWord.length)}",
                    "\n\u2022 ${getString(R.string.shortest_word)}: $shortestWord",
                    "\n\u2022 ${getString(R.string.shortest_word_length)}: ${numberFmt.format(shortestWord.length)}"
                )
            }
    }
    
    protected fun copyToClipboard(copiedText: CharSequence) {
        val clipData = ClipData.newPlainText("", copiedText)
        clipData.hideSensitiveContent()
        clipboardManager.setPrimaryClip(clipData)
        // Show snackbar only if 12L or lower to avoid duplicate notifications
        // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
        if (Build.VERSION.SDK_INT <= 32) {
            showSnackbar(
                getCoordinatorLayout(),
                requireContext().getString(R.string.copied_to_clipboard),
                getSnackbarAnchorView()
            )
        }
    }
    
    private fun generateNewFilename(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"))
        return "IYPS_export_${timestamp}.txt"
    }
    
    private fun getFormattedResultsText(): String {
        return buildString {
            if (!isPassphrase) {
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
                append("${
                    fragmentBinding.guessesSubtitle.text.replace(
                        // Insert ^ after "x 10" (before superscript)
                        Regex("\u00D7\\s+10(\\d+)"), "\u00D7 10\u005E$1"
                    )
                }\n\n")
                append("## ${getString(R.string.order_of_magn)}\n")
                append("${fragmentBinding.orderMagnSubtitle.text}\n\n")
                append("## ${getString(R.string.entropy)}\n")
                append("${fragmentBinding.entropySubtitle.text}\n\n")
                append("## ${getString(R.string.match_sequence)}\n")
                append("${fragmentBinding.matchSequenceSubtitle.text}\n\n")
                append("## ${getString(R.string.statistics)}\n")
                append("${fragmentBinding.statsSubtitle.text}")
            }
            else {
                append("# ${getString(R.string.passphrase)}\n")
                append("${fragmentBinding.passwordText.text.toString()}\n\n")
                append("## ${getString(R.string.entropy)}\n")
                append("${fragmentBinding.entropySubtitle.text}\n\n")
                append("## ${getString(R.string.statistics)}\n")
                append("${fragmentBinding.statsSubtitle.text}")
            }
        }
    }
    
    private val exportToFilePicker =
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
    
    // Subclasses must override
    protected abstract fun getSnackbarAnchorView(): View
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
