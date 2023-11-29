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
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.iyps.R
import com.iyps.appmanager.ApplicationManager
import com.iyps.common.EvaluatePassword
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.iyps.utils.ClipboardUtils.Companion.clearClipboard
import com.iyps.utils.ClipboardUtils.Companion.manageClipboard
import com.iyps.utils.ResultUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PasswordFragment : Fragment() {
    
    private var _binding: FragmentPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var clipboardManager: ClipboardManager
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val zxcvbn = (requireContext().applicationContext as ApplicationManager).zxcvbn
        clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        fragmentBinding.lengthSubtitle.text = "\u2022 ${getString(R.string.length)}"
        fragmentBinding.uppercaseSubtitle.text = "\u2022 ${getString(R.string.uppercase)}"
        fragmentBinding.lowercaseSubtitle.text = "\u2022 ${getString(R.string.lowercase)}"
        fragmentBinding.numbersSubtitle.text = "\u2022 ${getString(R.string.numbers)}"
        fragmentBinding.specialCharsSubtitle.text = "\u2022 ${getString(R.string.special_char)}"
        
        /*########################################################################################*/
        
        fragmentBinding.passwordText.apply {
            
            if (PreferenceManager(requireContext()).getBoolean(INCOG_KEYBOARD)) {
                imeOptions = IME_FLAG_NO_PERSONALIZED_LEARNING
                inputType = TYPE_TEXT_VARIATION_PASSWORD
            }
            
            addTextChangedListener(object : TextWatcher {
                
                var job: Job? = null
                val resultUtils = ResultUtils(requireContext())
                
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
                
                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    
                    // Introduce a subtle delay
                    // So passwords are checked after typing is finished
                    job?.cancel()
                    job =
                        lifecycleScope.launch {
                            delay(300)
                            if (charSequence.isNotEmpty()) {
                                EvaluatePassword(zxcvbn = zxcvbn,
                                                 password = charSequence,
                                                 fragmentPasswordBinding = fragmentBinding,
                                                 context = requireContext(),
                                                 resultUtils = resultUtils)
                            }
                            // If edit text is empty or cleared, reset everything
                            else {
                                resetDetails()
                            }
                        }
                }
                
                override fun afterTextChanged(editable: Editable) {}
            })
        }
        
        // Clipboard
        manageClipboard(clipboardManager)
    }
    
    // Reset details
    private fun resetDetails() {
        val notApplicableString = getString(R.string.na)
        val zeroString = getString(R.string.zero)
        val emptyMeterColor = resources.getColor(android.R.color.transparent, requireContext().theme)
        
        fragmentBinding.tenBGuessesStrength.text = notApplicableString
        fragmentBinding.tenKGuessesStrength.text = notApplicableString
        fragmentBinding.tenGuessesStrength.text = notApplicableString
        fragmentBinding.hundredGuessesStrength.text = notApplicableString
        fragmentBinding.tenBGuessesSubtitle.text = notApplicableString
        fragmentBinding.tenKGuessesSubtitle.text = notApplicableString
        fragmentBinding.tenGuessesSubtitle.text = notApplicableString
        fragmentBinding.hundredGuessesSubtitle.text = notApplicableString
        fragmentBinding.warningSubtitle.text = notApplicableString
        fragmentBinding.suggestionsSubtitle.text = notApplicableString
        fragmentBinding.guessesSubtitle.text = notApplicableString
        fragmentBinding.orderMagnSubtitle.text = notApplicableString
        fragmentBinding.orderMagnSubtitle.text = notApplicableString
        fragmentBinding.entropySubtitle.text = notApplicableString
        fragmentBinding.matchSequenceSubtitle.text = notApplicableString
        fragmentBinding.lengthText.text = zeroString
        fragmentBinding.uppercaseText.text = zeroString
        fragmentBinding.lowercaseText.text = zeroString
        fragmentBinding.numbersText.text = zeroString
        fragmentBinding.specialCharsText.text = zeroString
        
        fragmentBinding.tenBGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
        fragmentBinding.tenKGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
        fragmentBinding.tenGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
        fragmentBinding.hundredGuessesStrengthMeter.apply {
            setIndicatorColor(emptyMeterColor)
            setProgressCompat(0, true)
        }
        
    }
    
    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        clearClipboard(clipboardManager)
        _binding = null
    }
}