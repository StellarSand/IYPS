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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.FragmentGeneratePassphraseBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_CAPITALIZE
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_SEPARATOR
import com.iyps.preferences.PreferenceManager.Companion.PHRASE_WORDS
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.UiUtils.Companion.showSnackbar
import com.iyps.utils.UiUtils.Companion.setSliderThumbColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class GeneratePassphraseFragment : Fragment() {
    
    private var _binding: FragmentGeneratePassphraseBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var passphraseWordsMap: Map<String, String>
    private lateinit var secureRandom: SecureRandom
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentGeneratePassphraseBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val appManager = (requireContext().applicationContext as ApplicationManager)
        val mainActivity = requireActivity() as MainActivity
        preferenceManager = appManager.preferenceManager
        passphraseWordsMap = appManager.passphraseWordsMap
        secureRandom = appManager.secureRandom
        
        // Password length slider
        fragmentBinding.phraseWordsSlider.apply {
            value = preferenceManager.getFloatDefVal5(PHRASE_WORDS)
            fragmentBinding.wordsText.text = "${getString(R.string.words)}: ${value.toInt()}"
            setSliderThumbColor(requireContext(), this, 3f, value)
            
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                
                override fun onStopTrackingTouch(slider: Slider) {
                    generatePassphrase()
                }
                
            })
            
            addOnChangeListener { slider, value, _ ->
                fragmentBinding.wordsText.text = "${getString(R.string.words)}: ${slider.value.toInt()}"
                setSliderThumbColor(requireContext(), slider, 3f, value)
            }
        }
        
        // Separator dropdown
        fragmentBinding.separatorText.text = getString(R.string.separator).removePrefix("\u2022 ")
        val allSeparatorsDropdownList = listOf("-", ".", ",", getString(R.string.spaces))
        fragmentBinding.separatorDropdownMenu.apply {
            setText(preferenceManager.getString(PHRASE_SEPARATOR))
            
            val adapter = ArrayAdapter(requireContext(),
                                       R.layout.item_dropdown_menu,
                                       allSeparatorsDropdownList)
            setAdapter(adapter)
            
            setOnItemClickListener { _, _, _, _ ->
                generatePassphrase()
            }
        }
        
        // Capitalize
        fragmentBinding.capitalizeSwitch.apply {
            isChecked = preferenceManager.getBoolean(PHRASE_CAPITALIZE)
            setOnCheckedChangeListener { _, _ ->
                generatePassphrase()
            }
        }
        
        generatePassphrase()
        
        // Copy
        fragmentBinding.phraseCopyBtn.setOnClickListener {
            val clipData = ClipData.newPlainText("", fragmentBinding.phraseGeneratedTextView.text)
            hideSensitiveContent(clipData)
            (requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
            // Only show snackbar in 12L or lower to avoid duplicate notifications
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
            if (Build.VERSION.SDK_INT <= 32) {
                showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                             requireContext().getString(R.string.copied_to_clipboard),
                             mainActivity.activityBinding.mainBottomNav)
            }
        }
        
        // Regenerate
        fragmentBinding.phraseRegenerateBtn.setOnClickListener {
            generatePassphrase()
        }
        
        // Share
        fragmentBinding.phraseShareBtn.setOnClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                                                   .setType("text/plain")
                                                   .putExtra(Intent.EXTRA_TEXT, fragmentBinding.phraseGeneratedTextView.text),
                                               getString(R.string.share)))
        }
    }
    
    fun generatePassphrase() {
        val numberOfWords = fragmentBinding.phraseWordsSlider.value.toInt()
        lifecycleScope.launch(Dispatchers.Default) {
            val passphrase = buildString {
                for (i in 0 until numberOfWords) {
                    val dieRollsValues =
                        IntArray(5) { secureRandom.nextInt(6) + 1 } // Rolling a six-sided die five times.
                    val wordKey = dieRollsValues.joinToString("") // Form a string key
                    var word =
                        passphraseWordsMap[wordKey] // Find the word from words list with corresponding key
                    
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
        preferenceManager.apply {
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