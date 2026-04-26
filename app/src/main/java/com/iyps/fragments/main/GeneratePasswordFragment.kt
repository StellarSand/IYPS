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
import android.content.Context.CLIPBOARD_SERVICE
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
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.activities.MainActivity
import com.iyps.bottomsheets.GenerateMultipleBottomSheet
import com.iyps.databinding.FragmentGeneratePasswordBinding
import com.iyps.objects.GenerateMultiList
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.PWD_AMB_CHARS
import com.iyps.preferences.PreferenceManager.Companion.PWD_EXT_CHARS
import com.iyps.preferences.PreferenceManager.Companion.PWD_LOWERCASE
import com.iyps.preferences.PreferenceManager.Companion.PWD_NUMBERS
import com.iyps.preferences.PreferenceManager.Companion.PWD_LENGTH
import com.iyps.preferences.PreferenceManager.Companion.PWD_SPACES
import com.iyps.preferences.PreferenceManager.Companion.PWD_SPEC_CHARS
import com.iyps.preferences.PreferenceManager.Companion.PWD_UPPERCASE
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.IntentUtils.Companion.shareText
import com.iyps.utils.TextUtils.Companion.SPECIAL_CHARS
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

class GeneratePasswordFragment : Fragment() {
    
    private var _binding: FragmentGeneratePasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private val prefManager by inject<PreferenceManager>()
    private lateinit var uppercaseSwitch: MaterialSwitch
    private lateinit var lowercaseSwitch: MaterialSwitch
    private lateinit var extCharsSwitch: MaterialSwitch
    private lateinit var numbersSwitch: MaterialSwitch
    private lateinit var specialCharsSwitch: MaterialSwitch
    private lateinit var avoidAmbCharsSwitch: MaterialSwitch
    private lateinit var includeSpaceSwitch: MaterialSwitch
    private lateinit var primarySwitchesList: Array<MaterialSwitch>
    private lateinit var primarySwitchesPrefMap: Map<MaterialSwitch, String>
    private var uppercaseWithoutAmbChars = ""
    private var lowercaseWithoutAmbChars = ""
    private var numbersWithoutAmbChars = ""
    private var sliderValue = 0.0f
    private val secureRandom by inject<SecureRandom>()
    private var generatedPwdString: String = ""
    
    private companion object {
        private const val UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private const val UPPERCASE_EXT_CHARS = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝß"
        private const val LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz"
        private const val LOWERCASE_EXT_CHARS = "àáâãäåæçèéêëìíîïñòóôõöøùúûüýÿ"
        private const val NUMBERS = "0123456789"
        private const val UPPERCASE_AMB_CHARS = "ILOSBZ" // 0Oo, 1IlL, 2Z, 5S, 8B
        private const val LOWERCASE_AMB_CHARS = "loz"
        private const val NUM_AMB_CHARS = "01258"
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentGeneratePasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val mainActivity = requireActivity() as MainActivity
        sliderValue = prefManager.getFloat(PWD_LENGTH)
        uppercaseSwitch = fragmentBinding.uppercaseSwitch
        lowercaseSwitch = fragmentBinding.lowercaseSwitch
        numbersSwitch = fragmentBinding.numbersSwitch
        specialCharsSwitch = fragmentBinding.specialCharsSwitch
        extCharsSwitch = fragmentBinding.extCharsSwitch
        avoidAmbCharsSwitch = fragmentBinding.avoidAmbCharsSwitch
        includeSpaceSwitch = fragmentBinding.includeSpacesSwitch
        
        primarySwitchesList = arrayOf(uppercaseSwitch,
                                      lowercaseSwitch,
                                      numbersSwitch,
                                      specialCharsSwitch)
        
        primarySwitchesPrefMap = mapOf(uppercaseSwitch to PWD_UPPERCASE,
                                       lowercaseSwitch to PWD_LOWERCASE,
                                       numbersSwitch to PWD_NUMBERS,
                                       specialCharsSwitch to PWD_SPEC_CHARS)
        
        // Adjust scrollview for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.pwdScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left, top = insets.top, right = insets.right)
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 64f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        // Password length slider
        fragmentBinding.pwdLengthSlider.apply {
            value = sliderValue
            fragmentBinding.pwdLengthText.text = "${getString(R.string.length)}: ${value.toInt()}"
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                override fun onStopTrackingTouch(slider: Slider) {
                    if (slider.value != sliderValue) {
                        sliderValue = slider.value
                        showGeneratedPassword()
                    }
                }
            })
            addOnChangeListener { _, value, _ ->
                fragmentBinding.pwdLengthText.text = "${getString(R.string.length)}: ${value.toInt()}"
            }
        }
        
        // Switches
        // At least one switch must be enabled at all times (excluding ambiguous chars switch)
        primarySwitchesList.forEach { switch ->
            primarySwitchesPrefMap[switch]?.let { preferenceKey ->
                switch.isChecked = prefManager.getBoolean(preferenceKey)
            }
            switch.setOnCheckedChangeListener { checkedSwitch, _ ->
                val otherSwitches = primarySwitchesList.filter { it != switch }
                val otherSwitchesChecked = otherSwitches.any { it.isChecked }
                if (checkedSwitch == uppercaseSwitch || checkedSwitch == lowercaseSwitch) extCharsSwitch.enableOrDisableExtCharsSwitch()
                if (otherSwitchesChecked) showGeneratedPassword() else switch.isChecked = true
            }
        }
        
        avoidAmbCharsSwitch.apply {
            isChecked = prefManager.getBoolean(PWD_AMB_CHARS)
            setOnCheckedChangeListener { _, _ ->
                showGeneratedPassword()
            }
        }
        
        extCharsSwitch.apply {
            enableOrDisableExtCharsSwitch()
            isChecked = prefManager.getBoolean(PWD_EXT_CHARS, defValue = false)
            setOnCheckedChangeListener { _, _ ->
                showGeneratedPassword()
            }
        }
        
        includeSpaceSwitch.apply {
            isChecked = prefManager.getBoolean(PWD_SPACES, defValue = false)
            setOnCheckedChangeListener { _, _ ->
                showGeneratedPassword()
            }
        }
        
        showGeneratedPassword()
        
        // Details
        fragmentBinding.pwdDetailsBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), DetailsActivity::class.java)
                              .putExtra("PwdLine", generatedPwdString),
                          ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle())
        }
        
        // Copy
        fragmentBinding.pwdCopyBtn.setOnClickListener {
            val clipData = ClipData.newPlainText("", generatedPwdString)
            clipData.hideSensitiveContent()
            (requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
            // Show snackbar only if 12L or lower to avoid duplicate notifications
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
            if (Build.VERSION.SDK_INT <= 32) {
                showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                             requireContext().getString(R.string.copied_to_clipboard),
                             mainActivity.activityBinding.mainBottomNav)
            }
        }
        
        // Regenerate
        fragmentBinding.pwdRegenerateBtn.setOnClickListener {
            showGeneratedPassword()
        }
        
        // Share
        fragmentBinding.pwdShareBtn.setOnClickListener {
            requireActivity().shareText(generatedPwdString)
        }
        
        // Generate multiple
        fragmentBinding.pwdMultiGenBtn.setOnClickListener {
            lifecycleScope.launch {
                (1..7).map {
                    async { GenerateMultiList.multiList.add(generatePassword()) }
                }.awaitAll()
            }
            GenerateMultipleBottomSheet().show(parentFragmentManager, "GenerateMultipleBottomSheet")
        }
        
    }
    
    private fun MaterialSwitch.enableOrDisableExtCharsSwitch() {
        isEnabled = uppercaseSwitch.isChecked || lowercaseSwitch.isChecked
    }
    
    private fun getNonAmbChars(allChars: String, ambChars: String): String {
        return allChars.filterNot { it in ambChars }
    }
    
    private suspend fun generatePassword(): String {
        return withContext(Dispatchers.Default) {
            val allChars = buildList {
                if (uppercaseSwitch.isChecked) {
                    if (extCharsSwitch.isChecked) addAll(UPPERCASE_EXT_CHARS.asIterable())
                    if (avoidAmbCharsSwitch.isChecked) {
                        addAll(
                            uppercaseWithoutAmbChars.ifEmpty {
                                getNonAmbChars(UPPERCASE_CHARS, UPPERCASE_AMB_CHARS).also {
                                    uppercaseWithoutAmbChars = it
                                }
                            }.asIterable()
                        )
                    }
                    else addAll(UPPERCASE_CHARS.asIterable())
                }
                if (lowercaseSwitch.isChecked) {
                    if (extCharsSwitch.isChecked) addAll(LOWERCASE_EXT_CHARS.asIterable())
                    if (avoidAmbCharsSwitch.isChecked) {
                        addAll(
                            lowercaseWithoutAmbChars.ifEmpty {
                                getNonAmbChars(LOWERCASE_CHARS, LOWERCASE_AMB_CHARS).also {
                                    lowercaseWithoutAmbChars = it
                                }
                            }.asIterable()
                        )
                    }
                    else addAll(LOWERCASE_CHARS.asIterable())
                }
                if (numbersSwitch.isChecked) {
                    if (avoidAmbCharsSwitch.isChecked) {
                        addAll(
                            numbersWithoutAmbChars.ifEmpty {
                                getNonAmbChars(NUMBERS, NUM_AMB_CHARS).also {
                                    numbersWithoutAmbChars = it
                                }
                            }.asIterable()
                        )
                    }
                    else addAll(NUMBERS.asIterable())
                }
                if (specialCharsSwitch.isChecked) addAll(SPECIAL_CHARS.asIterable())
            }
            
            val length = sliderValue.toInt()
            val shouldAddSpaces = includeSpaceSwitch.isChecked
            val maxSpaces: Int
            val spacePositions = mutableSetOf<Int>()
            if (shouldAddSpaces) {
                maxSpaces = (length * 0.2).coerceAtMost(15.0).toInt()
                
                // To avoid spaces at the start & end,
                // iterate from 1 < (length - 1)
                for (index in (1 until (length - 1)).shuffled()) {
                    if (spacePositions.size >= maxSpaces) break
                    
                    // Prevent consecutive indexes for spaces
                    if (!spacePositions.contains(index - 1) && !spacePositions.contains(index + 1)) {
                        spacePositions.add(index)
                    }
                }
            }
            
            buildString(capacity = length) {
                (0 until length).forEach {
                    if (shouldAddSpaces && it in spacePositions) {
                        append(' ')
                    }
                    else {
                        val randomIndex = secureRandom.nextInt(allChars.size)
                        append(allChars[randomIndex])
                    }
                }
            }
        }
    }
    
    private fun showGeneratedPassword() {
        lifecycleScope.launch {
            generatedPwdString = generatePassword()
            fragmentBinding.pwdGeneratedTextView.setGenTextWithColor(generatedPwdString)
        }
    }
    
    override fun onPause() {
        super.onPause()
        prefManager.apply {
            setFloat(PWD_LENGTH, sliderValue)
            primarySwitchesList.forEach { switch ->
                primarySwitchesPrefMap[switch]?.let { preferenceKey ->
                    setBoolean(preferenceKey, switch.isChecked)
                }
            }
            setBoolean(PWD_AMB_CHARS, fragmentBinding.avoidAmbCharsSwitch.isChecked)
            setBoolean(PWD_SPACES, fragmentBinding.includeSpacesSwitch.isChecked)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}