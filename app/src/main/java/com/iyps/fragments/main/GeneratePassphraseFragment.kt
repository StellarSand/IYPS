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
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import com.iyps.activities.DetailsActivity
import com.iyps.activities.MainActivity
import com.iyps.bottomsheets.GenerateMultipleBottomSheet
import com.iyps.databinding.FragmentGeneratePassphraseBinding
import com.iyps.models.GenMultiItem
import com.iyps.models.GenPhraseDetails
import com.iyps.objects.AppState
import com.iyps.objects.GenerateMultiList
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_CAPITALIZE
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_NUMBERS
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_SEPARATOR_POS
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_WORDLIST_POS
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_WORDS_COUNT
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.ClipboardUtils.Companion.scheduleClipboardClear
import com.iyps.utils.IntentUtils.Companion.shareText
import com.iyps.utils.TextUtils.Companion.PHRASE_SEPARATORS
import com.iyps.utils.UiUtils.Companion.convertDpToPx
import com.iyps.utils.UiUtils.Companion.setGenTextWithColor
import com.iyps.utils.UiUtils.Companion.showSnackbar
import com.iyps.utils.UiUtils.Companion.showSupportAnimBtmSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.security.SecureRandom

class GeneratePassphraseFragment : Fragment() {
    
    private var _binding: FragmentGeneratePassphraseBinding? = null
    private val fragmentBinding get() = _binding!!
    private val prefManager by inject<PreferenceManager>()
    private var sliderValue = 0.0f
    private var wordListDropdownSelectedPos = 0
    private var separatorDropdownSelectedPos = 0
    private var allWordsArray = arrayOf<String>()
    private var totalWordsInWordlist = 0
    private var separator: String = ""
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
                getString(R.string.eff_short_2),
                getString(R.string.diceware_cs), // Czech
                getString(R.string.diceware_de_long), // German long
                getString(R.string.diceware_de_short), // German long
                getString(R.string.diceware_et), // Estonian
                getString(R.string.diceware_en), // English
                getString(R.string.diceware_es), // Spanish
                getString(R.string.diceware_fr), // French
                getString(R.string.diceware_it), // Italian
                getString(R.string.diceware_nl_1), // Dutch
                getString(R.string.diceware_nl_2), // Dutch
                getString(R.string.diceware_pt), // Portuguese
                getString(R.string.diceware_sv), // Swedish
                getString(R.string.diceware_tr), // Turkish
                getString(R.string.diceware_zh), // Chinese
                getString(R.string.diceware_ja) // Japanese
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
                        allWordsArray = getWordsArrayFromRes()
                        totalWordsInWordlist = allWordsArray.size
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
            setOnItemClickListener { parent, _, position, _ ->
                if (position != separatorDropdownSelectedPos) {
                    separatorDropdownSelectedPos = position
                    separator = getSeparator(parent.getItemAtPosition(position) as String)
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
            allWordsArray = getWordsArrayFromRes()
            totalWordsInWordlist = allWordsArray.size
            separator = getSeparator(fragmentBinding.separatorDropdownMenu.adapter.getItem(separatorDropdownSelectedPos) as String)
            showGeneratedPassphrase()
        }
        
        // Details
        fragmentBinding.phraseDetailsBtn.setOnClickListener {
            startActivity(
                Intent(requireActivity(), DetailsActivity::class.java)
                    .putExtra("PwdLine", generatedPhraseString)
                    .putExtra("isPassphrase", true)
                    .putExtra("phraseDetails", getPhraseDetails()),
                ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle()
            )
        }
        
        // Copy
        fragmentBinding.phraseCopyBtn.apply {
            setOnClickListener {
                val clipData = ClipData.newPlainText("", generatedPhraseString)
                clipData.hideSensitiveContent()
                (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
                // Show snackbar only if 12L or lower to avoid duplicate notifications
                // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
                if (Build.VERSION.SDK_INT <= 32) {
                    showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                 requireContext().getString(R.string.copied_to_clipboard),
                                 mainActivity.activityBinding.mainBottomNav)
                }
                scheduleClipboardClear(requireContext())
            }
        }
        
        // Regenerate
        fragmentBinding.phraseRegenerateBtn.setOnClickListener {
            showGeneratedPassphrase()
        }
        
        // Share
        fragmentBinding.phraseShareBtn.setOnClickListener {
            requireActivity().shareText(generatedPhraseString)
        }
        
        // Generate multiple
        fragmentBinding.phraseMultiGenBtn.setOnClickListener {
            lifecycleScope.launch {
                (1..7).map {
                    async {
                        GenerateMultiList.multiList.add(
                            GenMultiItem (
                                password = generatePassphrase(),
                                phraseDetails = getPhraseDetails()
                            )
                        )
                    }
                }.awaitAll()
                
                GenerateMultipleBottomSheet(isPassphraseFragment = true).show(parentFragmentManager, "GenerateMultipleBottomSheet")
            }
        }
    }
    
    private suspend fun getWordsArrayFromRes(): Array<String> {
        return withContext(Dispatchers.IO) {
            val wordlistRawRes =
                when (wordListDropdownSelectedPos) {
                    0 -> resources.openRawResource(R.raw.eff_passphrase_long)
                    1 -> resources.openRawResource(R.raw.eff_passphrase_short)
                    2 -> resources.openRawResource(R.raw.eff_passphrase_typo_tolerant)
                    3 -> resources.openRawResource(R.raw.diceware_cs)
                    4 -> resources.openRawResource(R.raw.diceware_de_long)
                    5 -> resources.openRawResource(R.raw.diceware_de_short)
                    6 -> resources.openRawResource(R.raw.diceware_et)
                    7 -> resources.openRawResource(R.raw.diceware_en)
                    8 -> resources.openRawResource(R.raw.diceware_es)
                    9 -> resources.openRawResource(R.raw.diceware_fr)
                    10 -> resources.openRawResource(R.raw.diceware_it)
                    11 -> resources.openRawResource(R.raw.diceware_nl_1)
                    12 -> resources.openRawResource(R.raw.diceware_nl_2)
                    13 -> resources.openRawResource(R.raw.diceware_pt)
                    14 -> resources.openRawResource(R.raw.diceware_sv)
                    15 -> resources.openRawResource(R.raw.diceware_tr)
                    16 -> resources.openRawResource(R.raw.diceware_zh)
                    else -> resources.openRawResource(R.raw.diceware_ja)
                }
            
            wordlistRawRes.bufferedReader().use { it.readLines() }.toTypedArray()
        }
    }
    
    private fun getSeparator(selectedItem: String): String {
        // Append zero width space (\u200B) to the separator
        // This will break/wrap the line after the separator,
        // instead of in the middle of a word
        return if (selectedItem == getString(R.string.spaces)) " \u200B" else "${selectedItem}\u200B"
    }
    
    private suspend fun generatePassphrase(): String {
        val wordsInPhrase = sliderValue.toInt()
        val shouldCapitalize = fragmentBinding.capitalizeSwitch.isChecked
        var shouldAddNumber = fragmentBinding.phraseNumbersSwitch.isChecked
        var numPosition = 0
        
        return withContext(Dispatchers.Default) {
            if (shouldAddNumber) {
                numPosition = secureRandom.nextInt(wordsInPhrase)
            }
            
            buildString {
                // Instead of rolling a six sided die 5 (or 4) times,
                // select random words from wordlist directly.
                // This will help eliminate some complexity as I cleaned the diceware wordlists,
                // therefore some diceware wordlists don't have 7776 (6^5) or 1296 (6^4) words.
                //
                // METHOD A (not done below):
                // (0..wordsInPhrase).forEach{ repeat(5) { secureRandom.nextInt(totalWordsInWordlist) } }
                // But this could result in some words being selected more than once.
                //
                // METHOD B (done below):
                // - Take the indices of the words from the wordlist
                // - Shuffle them randomly
                // - Limit to number of words in passphrase
                // - Select words using those indices from allWordsArray
                // This will result in unique words.
                (0 until totalWordsInWordlist)
                    .shuffled(secureRandom) // Randomly selected indices of the words from wordlist
                    .take(wordsInPhrase)
                    .forEachIndexed { /*index = */ posInPhrase, /*value = */ indexInWordlist ->
                        // posInPhrase = position of the word in passphrase
                        // indexInWordlist = index of the word from wordlist
                        var word = allWordsArray[indexInWordlist]
                        if (shouldCapitalize) {
                            word = word.replaceFirstChar { it.titlecase() }
                        }
                        append(word)
                        if (shouldAddNumber && numPosition == posInPhrase) {
                            append(secureRandom.nextInt(9) + 1) // Random number from 1-9
                            shouldAddNumber = false
                        }
                        if (posInPhrase < wordsInPhrase - 1) append(separator)
                    }
            }
        }
    }
    
    private fun showGeneratedPassphrase() {
        lifecycleScope.launch {
            generatedPhraseString = generatePassphrase()
            fragmentBinding.phraseGeneratedTextView.setGenTextWithColor(
                generatedString = generatedPhraseString,
                isPassphrase = true
            )
            if (AppState.showSupportBtmSheet) {
                showSupportAnimBtmSheet(parentFragmentManager)
            }
        }
    }
    
    private fun getPhraseDetails(): GenPhraseDetails {
        return GenPhraseDetails(
            wordsInPhrase = sliderValue,
            totalWordsInWordlist = totalWordsInWordlist,
            separator = separator,
            hasNumber = fragmentBinding.phraseNumbersSwitch.isChecked
        )
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