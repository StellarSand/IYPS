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
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.databinding.FragmentGeneratePassphraseBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_CAPITALIZE
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_SEPARATOR
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_WORDS
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.UiUtils.Companion.convertDpToPx
import com.iyps.utils.UiUtils.Companion.setButtonTooltipText
import com.iyps.utils.UiUtils.Companion.showSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.security.SecureRandom

class GeneratePassphraseFragment : Fragment() {
    
    private var _binding: FragmentGeneratePassphraseBinding? = null
    private val fragmentBinding get() = _binding!!
    private val prefManager by inject<PreferenceManager>()
    
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
        
        // Password length slider
        fragmentBinding.phraseWordsSlider.apply {
            value = prefManager.getFloat(PHRASE_WORDS, defValue = 5f)
            fragmentBinding.wordsText.text = "${getString(R.string.words)}: ${value.toInt()}"
            
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                
                override fun onStopTrackingTouch(slider: Slider) {
                    generatePassphrase()
                }
                
            })
            
            addOnChangeListener { _, value, _ ->
                fragmentBinding.wordsText.text = "${getString(R.string.words)}: ${value.toInt()}"
            }
        }
        
        // Separator dropdown
        fragmentBinding.separatorText.text = getString(R.string.separator).removePrefix("\u2022 ")
        fragmentBinding.separatorDropdownMenu.apply {
            setText(prefManager.getString(PHRASE_SEPARATOR))
            setAdapter(ArrayAdapter(requireContext(),
                                    R.layout.item_dropdown_menu,
                                    arrayOf("-", ".", ",", getString(R.string.spaces))))
            
            setOnItemClickListener { _, _, _, _ ->
                generatePassphrase()
            }
        }
        
        // Capitalize
        fragmentBinding.capitalizeSwitch.apply {
            isChecked = prefManager.getBoolean(PHRASE_CAPITALIZE)
            setOnCheckedChangeListener { _, _ ->
                generatePassphrase()
            }
        }
        
        generatePassphrase()
        
        // Copy
        fragmentBinding.phraseCopyBtn.apply {
            setButtonTooltipText(getString(R.string.copy))
            setOnClickListener {
                val clipData = ClipData.newPlainText("", fragmentBinding.phraseGeneratedTextView.text)
                clipData.hideSensitiveContent()
                (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
                // Only show snackbar in 12L or lower to avoid duplicate notifications
                // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
                if (Build.VERSION.SDK_INT <= 32) {
                    showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                 requireContext().getString(R.string.copied_to_clipboard),
                                 mainActivity.activityBinding.mainBottomNav)
                }
            }
        }
        
        // Regenerate
        fragmentBinding.phraseRegenerateBtn.apply {
            setButtonTooltipText(getString(R.string.regenerate))
            setOnClickListener {
                generatePassphrase()
            }
        }
        
        // Share
        fragmentBinding.phraseShareBtn.apply {
            setButtonTooltipText(getString(R.string.share))
            setOnClickListener {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                                                       .setType("text/plain")
                                                       .putExtra(Intent.EXTRA_TEXT, fragmentBinding.phraseGeneratedTextView.text),
                                                   getString(R.string.share)))
            }
        }
    }
    
    fun generatePassphrase() {
        val numberOfWords = fragmentBinding.phraseWordsSlider.value.toInt()
        lifecycleScope.launch(Dispatchers.Default) {
            val passphrase = buildString {
                for (i in 0 until numberOfWords) {
                    val dieRollsValues =
                        IntArray(5) { get<SecureRandom>().nextInt(6) + 1 } // Rolling a six-sided die five times.
                    val wordKey = dieRollsValues.joinToString("") // Form a string key
                    var word = get<Map<String, String>>()[wordKey] // Find the word from words list with corresponding key
                    
                    if (fragmentBinding.capitalizeSwitch.isChecked) {
                        word =
                            word?.replaceFirstChar { char ->
                                char.titlecase()
                            }
                    }
                    
                    append(word)
                    if (i < numberOfWords - 1) {
                        append(
                            if (fragmentBinding.separatorDropdownMenu.text.toString() == getString(R.string.spaces)) " "
                            else fragmentBinding.separatorDropdownMenu.text.toString()
                        )
                    }
                }
            }
            
            withContext(Dispatchers.Main) {
                fragmentBinding.phraseGeneratedTextView.text = passphrase
            }
        }
        
    }
    
    override fun onPause() {
        super.onPause()
        prefManager.apply {
            setFloat(PHRASE_WORDS, fragmentBinding.phraseWordsSlider.value)
            setBoolean(PHRASE_CAPITALIZE, fragmentBinding.capitalizeSwitch.isChecked)
            setString(PHRASE_SEPARATOR, fragmentBinding.separatorDropdownMenu.text.toString())
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}