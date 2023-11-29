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
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.slider.Slider.OnSliderTouchListener
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.activities.MainActivity
import com.iyps.databinding.FragmentGenerateBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.AMB_CHARS
import com.iyps.preferences.PreferenceManager.Companion.LOWERCASE
import com.iyps.preferences.PreferenceManager.Companion.NUMBERS
import com.iyps.preferences.PreferenceManager.Companion.PASS_LENGTH
import com.iyps.preferences.PreferenceManager.Companion.SPACES
import com.iyps.preferences.PreferenceManager.Companion.SPEC_CHARS
import com.iyps.preferences.PreferenceManager.Companion.UPPERCASE
import java.security.SecureRandom

class GenerateFragment : Fragment() {
    
    private var _binding: FragmentGenerateBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var uppercaseSwitch: MaterialSwitch
    private lateinit var lowercaseSwitch: MaterialSwitch
    private lateinit var numbersSwitch: MaterialSwitch
    private lateinit var specialCharsSwitch: MaterialSwitch
    private lateinit var avoidAmbCharsSwitch: MaterialSwitch
    private lateinit var includeSpaceSwitch: MaterialSwitch
    private lateinit var primarySwitchesList: List<MaterialSwitch>
    private lateinit var  primarySwitchesPrefMap: Map<MaterialSwitch, String>
    private var uppercaseWithoutAmbChars = ""
    private var lowercaseWithoutAmbChars = ""
    private var numbersWithoutAmbChars = ""
    
    companion object {
        val uppercaseChars = ('A'..'Z').joinToString("")
        val lowercaseChars = ('a'..'z').joinToString("")
        val numbers = ('0'..'9').joinToString("")
        const val specialChars = "!@#$%^&*+_-<.>="
        const val uppercaseAmbChars = "ILOSB"
        const val lowercaseAmbChars = "ilo"
        const val numbersAmbChars = "058"
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentGenerateBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val mainActivity = requireActivity() as MainActivity
        preferenceManager = PreferenceManager(requireContext())
        uppercaseSwitch = fragmentBinding.uppercaseSwitch
        lowercaseSwitch = fragmentBinding.lowercaseSwitch
        numbersSwitch = fragmentBinding.numbersSwitch
        specialCharsSwitch = fragmentBinding.specialCharsSwitch
        avoidAmbCharsSwitch = fragmentBinding.avoidAmbCharsSwitch
        includeSpaceSwitch = fragmentBinding.includeSpacesSwitch
        
        primarySwitchesList = listOf(uppercaseSwitch,
                                     lowercaseSwitch,
                                     numbersSwitch,
                                     specialCharsSwitch)
        
        primarySwitchesPrefMap = mapOf(uppercaseSwitch to UPPERCASE,
                                       lowercaseSwitch to LOWERCASE,
                                       numbersSwitch to NUMBERS,
                                       specialCharsSwitch to SPEC_CHARS)
        
        // Password length slider
        fragmentBinding.passwordLengthSlider.apply {
            value = preferenceManager.getFloat(PASS_LENGTH)
            fragmentBinding.lengthText.text = "${getString(R.string.length)}: ${value.toInt()}"
            setSliderThumbColor(this, value)
            
            addOnSliderTouchListener(object : OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                
                override fun onStopTrackingTouch(slider: Slider) {
                    generatePassword()
                }
                
            })
            
            addOnChangeListener { slider, value, _ ->
                fragmentBinding.lengthText.text = "${getString(R.string.length)}: ${slider.value.toInt()}"
                setSliderThumbColor(slider, value)
            }
        }
        
        // Switches
        // At least one switch must be enabled at all times (excluding ambiguous chars switch)
        primarySwitchesList.forEach { switch ->
            primarySwitchesPrefMap[switch]?.let { preferenceKey ->
                switch.isChecked = preferenceManager.getBoolean(preferenceKey)
            }
            switch.setOnCheckedChangeListener { _, _ ->
                val otherSwitches = primarySwitchesList.filter { it != switch }
                val otherSwitchesChecked = otherSwitches.any { it.isChecked }
                if (otherSwitchesChecked) {
                    generatePassword()
                }
                else {
                    switch.isChecked = true
                }
            }
        }
        avoidAmbCharsSwitch.apply {
            isChecked = preferenceManager.getBoolean(AMB_CHARS)
            setOnCheckedChangeListener { _, _ ->
                generatePassword()
            }
        }
        includeSpaceSwitch.apply {
            isChecked = preferenceManager.getBooleanDefValFalse(SPACES)
            setOnCheckedChangeListener { _, _ ->
                generatePassword()
            }
        }
        
        generatePassword()
        
        // Details
        fragmentBinding.detailsBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), DetailsActivity::class.java)
                              .putExtra("PwdLine", fragmentBinding.generatedPasswordTextView.text))
        }
        
        // Copy
        fragmentBinding.copyPasswordBtn.setOnClickListener {
            val clipData = ClipData.newPlainText("Generated password",
                                                 fragmentBinding.generatedPasswordTextView.text)
            
            (requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                .setPrimaryClip(clipData)
            
            Snackbar.make(mainActivity.activityBinding.mainCoordLayout,
                          getString(R.string.copied_to_clipboard),
                          BaseTransientBottomBar.LENGTH_SHORT)
                .setAnchorView(mainActivity.activityBinding.mainBottomNav)
                .show()
        }
        
        // Regenerate
        fragmentBinding.regenerateBtn.setOnClickListener {
            generatePassword()
        }
        
        // Share
        fragmentBinding.shareBtn.setOnClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                                                   .setType("text/plain")
                                                   .putExtra(Intent.EXTRA_TEXT, fragmentBinding.generatedPasswordTextView.text),
                                               getString(R.string.share)))
        }
        
    }
    
    private fun setSliderThumbColor(slider: Slider, value: Float) {
        val sliderThumbColor =
            if (value == 5f) {
                requireContext().getColor(R.color.color_primary)
            }
            else {
                requireContext().getColor(R.color.color_onPrimary)
            }
        
        slider.thumbTintList = ColorStateList.valueOf(sliderThumbColor)
    }
    
    private fun getNonAmbChars(allChars: String, ambChars: String): String {
        return allChars.filterNot { it in ambChars }
    }
    
    private fun generatePassword() {
        val allChars = buildString {
            if (uppercaseSwitch.isChecked) {
                append(
                    if (avoidAmbCharsSwitch.isChecked) uppercaseWithoutAmbChars.ifEmpty {
                        // Only generate non-ambiguous chars if not done already.
                        // This would avoid unnecessary generations everytime this function is called.
                        getNonAmbChars(uppercaseChars, uppercaseAmbChars).also { uppercaseWithoutAmbChars = it }
                    }
                    else uppercaseChars
                )
            }
            if (lowercaseSwitch.isChecked) {
                append(
                    if (avoidAmbCharsSwitch.isChecked) lowercaseWithoutAmbChars.ifEmpty {
                        getNonAmbChars(lowercaseChars, lowercaseAmbChars).also { lowercaseWithoutAmbChars = it }
                    }
                    else lowercaseChars
                )
            }
            if (numbersSwitch.isChecked) {
                append(
                    if (avoidAmbCharsSwitch.isChecked) numbersWithoutAmbChars.ifEmpty {
                        getNonAmbChars(numbers, numbersAmbChars).also { numbersWithoutAmbChars = it }
                    }
                    else numbers
                )
            }
            if (specialCharsSwitch.isChecked) append(specialChars)
        }
        
        val password = buildString {
            val length = fragmentBinding.passwordLengthSlider.value.toInt()
            val spacesToInsert =
                if (includeSpaceSwitch.isChecked) SecureRandom().nextInt(length)
                else 0
            
            for (i in 0 until length) {
                if (includeSpaceSwitch.isChecked
                    && i in 1 until (length - 1) // Avoid spaces at the beginning & end
                    && i < spacesToInsert) {
                    append(' ')
                }
                else {
                    val randomIndex = SecureRandom().nextInt(allChars.length)
                    append(allChars[randomIndex])
                }
            }
        }
        
        fragmentBinding.generatedPasswordTextView.text = password
    }
    
    override fun onPause() {
        super.onPause()
        preferenceManager.apply {
            setFloat(PASS_LENGTH, fragmentBinding.passwordLengthSlider.value)
            primarySwitchesList.forEach { switch ->
                primarySwitchesPrefMap[switch]?.let { preferenceKey ->
                    setBoolean(preferenceKey, switch.isChecked)
                }
            }
            setBoolean(AMB_CHARS, fragmentBinding.avoidAmbCharsSwitch.isChecked)
            setBoolean(SPACES, fragmentBinding.includeSpacesSwitch.isChecked)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}