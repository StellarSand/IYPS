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

package com.iyps.fragments.main

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.bottomsheets.GenerateMultipleBottomSheet
import com.iyps.databinding.FragmentGeneratePassphraseBinding
import com.iyps.objects.GenerateMultiList
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_CAPITALIZE
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_NUMBERS
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_SEPARATOR_POS
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_WORDLIST_POS
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_WORDS_COUNT
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.IntentUtils.Companion.shareText
import com.iyps.utils.TextUtils.Companion.PHRASE_SEPARATORS
import com.iyps.utils.UiUtils.Companion.convertDpToPx
import com.iyps.utils.UiUtils.Companion.setGenTextWithColor
import com.iyps.utils.UiUtils.Companion.showSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.security.SecureRandom
import kotlin.sequences.forEach

class GeneratePassphraseFragment : Fragment() {
    
    private var _binding: FragmentGeneratePassphraseBinding? = null
    private val fragmentBinding get() = _binding!!
    private val prefManager by inject<PreferenceManager>()
    private var sliderValue = 0.0f
    private var wordListDropdownSelectedPos = 0
    private var separatorDropdownSelectedPos = 0
    private var wordMap = mapOf<String, String>()
    private val secureRandom by inject<SecureRandom>()
    private var generatedPhraseString: String = ""
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentGeneratePassphraseBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val mainActivity = requireActivity() as MainActivity
        sliderValue = prefManager.getFloat(PHRASE_WORDS_COUNT, defValue = 6f)
        val wordlistDropdownArray =
            arrayOf(
                getString(R.string.eff_long),
                getString(R.string.eff_short_1),
                getString(R.string.eff_short_2)
            )
        wordListDropdownSelectedPos = prefManager.getInt(PHRASE_WORDLIST_POS)
        val separatorDropdownArray = PHRASE_SEPARATORS.map { it.toString() }.toTypedArray() + getString(R.string.spaces)
        separatorDropdownSelectedPos = prefManager.getInt(PHRASE_SEPARATOR_POS)
        
        // Adjust scrollview for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.phraseScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 64f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        // Words slider
        fragmentBinding.phraseWordsSlider.apply {
            value = sliderValue
            fragmentBinding.wordsText.text = "${getString(R.string.words)}: ${value.toInt()}"
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                override fun onStopTrackingTouch(slider: Slider) {
                    if (slider.value != sliderValue) {
                        sliderValue = slider.value
                        showGeneratedPassphrase()
                    }
                }
                
            })
            addOnChangeListener { _, value, _ ->
                fragmentBinding.wordsText.text = "${getString(R.string.words)}: ${value.toInt()}"
            }
        }
        
        // Wordlist dropdown
        (fragmentBinding.wordlistDropdownMenu as MaterialAutoCompleteTextView).apply {
            setText(wordlistDropdownArray[wordListDropdownSelectedPos])
            setSimpleItems(wordlistDropdownArray)
            setOnItemClickListener { _, _, position, _ ->
                if (position != wordListDropdownSelectedPos) {
                    wordListDropdownSelectedPos = position
                    lifecycleScope.launch {
                        wordMap = getWordMapFromRes()
                        showGeneratedPassphrase()
                    }
                }
            }
        }
        
        // Separator dropdown
        fragmentBinding.separatorTextInputLayout.hint = getString(R.string.separator).removePrefix("\u2022 ")
        (fragmentBinding.separatorDropdownMenu as MaterialAutoCompleteTextView).apply {
            setText(separatorDropdownArray[separatorDropdownSelectedPos])
            setSimpleItems(separatorDropdownArray)
            setOnItemClickListener { _, _, position, _ ->
                if (position != separatorDropdownSelectedPos) {
                    separatorDropdownSelectedPos = position
                    showGeneratedPassphrase()
                }
            }
        }
        
        // Capitalize
        fragmentBinding.capitalizeSwitch.apply {
            isChecked = prefManager.getBoolean(PHRASE_CAPITALIZE)
            setOnCheckedChangeListener { _, _ ->
                showGeneratedPassphrase()
            }
        }
        
        // Numbers
        fragmentBinding.phraseNumbersSwitch.apply {
            isChecked = prefManager.getBoolean(PHRASE_NUMBERS, defValue = false)
            setOnCheckedChangeListener { _, _ ->
                showGeneratedPassphrase()
            }
        }
        
        lifecycleScope.launch {
            wordMap = getWordMapFromRes()
            showGeneratedPassphrase()
        }
        
        // Copy
        fragmentBinding.phraseCopyBtn.apply {
            setOnClickListener {
                val clipData = ClipData.newPlainText("", fragmentBinding.phraseGeneratedTextView.text)
                clipData.hideSensitiveContent()
                (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
                // Show snackbar only if 12L or lower to avoid duplicate notifications
                // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
                if (Build.VERSION.SDK_INT <= 32) {
                    showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                 requireContext().getString(R.string.copied_to_clipboard),
                                 mainActivity.activityBinding.mainBottomNav)
                }
            }
        }
        
        // Regenerate
        fragmentBinding.phraseRegenerateBtn.setOnClickListener {
            showGeneratedPassphrase()
        }
        
        // Share
        fragmentBinding.phraseShareBtn.setOnClickListener {
            requireActivity().shareText(fragmentBinding.phraseGeneratedTextView.text.toString())
        }
        
        // Generate multiple
        fragmentBinding.phraseMultiGenBtn.setOnClickListener {
            lifecycleScope.launch {
                (1..7).map {
                    async { GenerateMultiList.multiList.add(generatePassphrase()) }
                }.awaitAll()
            }
            GenerateMultipleBottomSheet(isPassphraseFragment = true).show(parentFragmentManager, "GenerateMultipleBottomSheet")
        }
    }
    
    private suspend fun getWordMapFromRes(): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val wordlistWordMap = hashMapOf<String, String>()
            val wordlistRawRes =
                when (wordListDropdownSelectedPos) {
                    0 -> resources.openRawResource(R.raw.eff_passphrase_long)
                    1 -> resources.openRawResource(R.raw.eff_passphrase_short)
                    else -> resources.openRawResource(R.raw.eff_passphrase_typo_tolerant)
                }
            
            wordlistRawRes
                .bufferedReader()
                .useLines { lines ->
                    lines.forEach { line ->
                        val (id, word) = line.split("\t")
                        wordlistWordMap[id] = word
                    }
                }
            
            wordlistWordMap as Map<String, String>
        }
    }
    
    private suspend fun generatePassphrase(): String {
        val wordsCount = sliderValue.toInt()
        val repeatTimes = if (wordListDropdownSelectedPos == 0) 5 else 4
        val shouldCapitalize = fragmentBinding.capitalizeSwitch.isChecked
        val shouldAddNumbers = fragmentBinding.phraseNumbersSwitch.isChecked
        val maxNums: Int
        var numPositions: Set<Int>? = null
        
        // Append zero width space (\u200B) to the separator
        // This will break/wrap the line after the separator,
        // instead of in the middle of a word
        val separator =
            if (fragmentBinding.separatorDropdownMenu.text.toString() == getString(R.string.spaces)) " \u200B"
            else "${fragmentBinding.separatorDropdownMenu.text}\u200B"
        
        return withContext(Dispatchers.Default) {
            if (shouldAddNumbers) {
                maxNums = (wordsCount * 0.4).coerceAtMost(8.0).toInt()
                numPositions = (0 until wordsCount).shuffled().take(maxNums).toSet()
            }
            
            buildString {
                (0 until wordsCount).forEach {
                    val wordKey =
                        buildString(capacity = repeatTimes) {
                            // Rolling a six-sided die five (or four) times
                            repeat(repeatTimes) {
                                append(secureRandom.nextInt(6) + 1)
                            }
                        }
                    var word = wordMap[wordKey] // Find the word from words list with corresponding key
                    if (shouldCapitalize) {
                        word =
                            word?.replaceFirstChar { char ->
                                char.titlecase()
                            }
                    }
                    append(word)
                    if (shouldAddNumbers && numPositions?.contains(it) == true) {
                        append(secureRandom.nextInt(9) + 1) // Random number from 1-9
                    }
                    if (it < wordsCount - 1) append(separator)
                }
            }
        }
    }
    
    private fun showGeneratedPassphrase() {
        lifecycleScope.launch {
            generatedPhraseString = generatePassphrase()
            fragmentBinding.phraseGeneratedTextView.setGenTextWithColor(
                generatedString = generatedPhraseString,
                isPassphrase = true)
        }
    }
    
    override fun onPause() {
        super.onPause()
        prefManager.apply {
            setFloat(PHRASE_WORDS_COUNT, sliderValue)
            setInt(PHRASE_WORDLIST_POS, wordListDropdownSelectedPos)
            setInt(PHRASE_SEPARATOR_POS, separatorDropdownSelectedPos)
            setBoolean(PHRASE_CAPITALIZE, fragmentBinding.capitalizeSwitch.isChecked)
            setBoolean(PHRASE_NUMBERS, fragmentBinding.phraseNumbersSwitch.isChecked)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}