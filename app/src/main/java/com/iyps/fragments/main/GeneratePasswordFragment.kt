package com.iyps.fragments.main

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.activities.MainActivity
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.FragmentGeneratePasswordBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.PWD_AMB_CHARS
import com.iyps.preferences.PreferenceManager.Companion.PWD_LOWERCASE
import com.iyps.preferences.PreferenceManager.Companion.PWD_NUMBERS
import com.iyps.preferences.PreferenceManager.Companion.PWD_LENGTH
import com.iyps.preferences.PreferenceManager.Companion.PWD_RPT_CHARS
import com.iyps.preferences.PreferenceManager.Companion.PWD_SPACES
import com.iyps.preferences.PreferenceManager.Companion.PWD_SPEC_CHARS
import com.iyps.preferences.PreferenceManager.Companion.PWD_UPPERCASE
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.ClipboardUtils.Companion.showCopiedSnackbar
import com.iyps.utils.UiUtils.Companion.setSliderThumbColor
import java.security.SecureRandom

class GeneratePasswordFragment : Fragment() {
    
    private var _binding: FragmentGeneratePasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var uppercaseSwitch: MaterialSwitch
    private lateinit var lowercaseSwitch: MaterialSwitch
    private lateinit var numbersSwitch: MaterialSwitch
    private lateinit var specialCharsSwitch: MaterialSwitch
    private lateinit var avoidAmbCharsSwitch: MaterialSwitch
    private lateinit var repeatCharsSwitch: MaterialSwitch
    private lateinit var includeSpaceSwitch: MaterialSwitch
    private lateinit var primarySwitchesList: List<MaterialSwitch>
    private lateinit var primarySwitchesPrefMap: Map<MaterialSwitch, String>
    private var uppercaseWithoutAmbChars = ""
    private var lowercaseWithoutAmbChars = ""
    private var numbersWithoutAmbChars = ""
    private lateinit var secureRandom: SecureRandom
    
    companion object {
        val uppercaseChars = ('A'..'Z').joinToString("")
        val lowercaseChars = ('a'..'z').joinToString("")
        val numbers = ('0'..'9').joinToString("")
        const val specialChars = "!@#$%^&*+_-.="
        const val uppercaseAmbChars = "ILOSB"
        const val lowercaseAmbChars = "ilo"
        const val numbersAmbChars = "058"
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
        val appManager = (requireContext().applicationContext as ApplicationManager)
        preferenceManager = appManager.preferenceManager
        secureRandom = appManager.secureRandom
        
        uppercaseSwitch = fragmentBinding.uppercaseSwitch
        lowercaseSwitch = fragmentBinding.lowercaseSwitch
        numbersSwitch = fragmentBinding.numbersSwitch
        specialCharsSwitch = fragmentBinding.specialCharsSwitch
        avoidAmbCharsSwitch = fragmentBinding.avoidAmbCharsSwitch
        repeatCharsSwitch = fragmentBinding.repeatCharsSwitch
        includeSpaceSwitch = fragmentBinding.includeSpacesSwitch
        
        primarySwitchesList = listOf(uppercaseSwitch,
                                     lowercaseSwitch,
                                     numbersSwitch,
                                     specialCharsSwitch)
        
        primarySwitchesPrefMap = mapOf(uppercaseSwitch to PWD_UPPERCASE,
                                       lowercaseSwitch to PWD_LOWERCASE,
                                       numbersSwitch to PWD_NUMBERS,
                                       specialCharsSwitch to PWD_SPEC_CHARS)
        
        // Password length slider
        fragmentBinding.pwdLengthSlider.apply {
            value = preferenceManager.getFloatDefVal20(PWD_LENGTH)
            fragmentBinding.lengthText.text = "${getString(R.string.length)}: ${value.toInt()}"
            setSliderThumbColor(requireContext(), this, 5f, value)
            
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}
                
                override fun onStopTrackingTouch(slider: Slider) {
                    generatePassword()
                }
                
            })
            
            addOnChangeListener { slider, value, _ ->
                fragmentBinding.lengthText.text = "${getString(R.string.length)}: ${slider.value.toInt()}"
                setSliderThumbColor(requireContext(), slider, 5f, value)
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
            isChecked = preferenceManager.getBoolean(PWD_AMB_CHARS)
            setOnCheckedChangeListener { _, _ ->
                generatePassword()
            }
        }
        repeatCharsSwitch.apply {
            isChecked = preferenceManager.getBoolean(PWD_RPT_CHARS)
            setOnCheckedChangeListener { _, _ ->
                generatePassword()
            }
        }
        includeSpaceSwitch.apply {
            isChecked = preferenceManager.getBooleanDefValFalse(PWD_SPACES)
            setOnCheckedChangeListener { _, _ ->
                generatePassword()
            }
        }
        
        generatePassword()
        
        // Details
        fragmentBinding.pwdDetailsBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), DetailsActivity::class.java)
                              .putExtra("PwdLine", fragmentBinding.pwdGeneratedTextView.text))
        }
        
        // Copy
        fragmentBinding.pwdCopyBtn.setOnClickListener {
            val clipData = ClipData.newPlainText("", fragmentBinding.pwdGeneratedTextView.text)
            hideSensitiveContent(clipData)
            (requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
            // Only show snackbar in 12L or lower to avoid duplicate notifications
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
            if (Build.VERSION.SDK_INT <= 32) {
                showCopiedSnackbar(requireContext(),
                                   mainActivity.activityBinding.mainCoordLayout,
                                   mainActivity.activityBinding.mainBottomNav)
            }
        }
        
        // Regenerate
        fragmentBinding.pwdRegenerateBtn.setOnClickListener {
            generatePassword()
        }
        
        // Share
        fragmentBinding.pwdShareBtn.setOnClickListener {
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                                                   .setType("text/plain")
                                                   .putExtra(Intent.EXTRA_TEXT, fragmentBinding.pwdGeneratedTextView.text),
                                               getString(R.string.share)))
        }
        
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
            val length = fragmentBinding.pwdLengthSlider.value.toInt()
            val maxSpaces = (length * 0.2).coerceAtMost(15.0).toInt()
            
            for (i in 0 until length) {
                if (includeSpaceSwitch.isChecked
                    && i in 1 until (length - 1) // Avoid spaces at the beginning & end
                    && secureRandom.nextInt(length + 1) < maxSpaces
                    && this[i - 1] != ' ') {
                    append(' ')
                }
                else {
                    val randomIndex = secureRandom.nextInt(allChars.length)
                    var generatedChar = allChars[randomIndex]
                    when {
                        !repeatCharsSwitch.isChecked && i > 0 -> {
                            while (contains(generatedChar, true)) {
                                val newIndex = secureRandom.nextInt(allChars.length)
                                generatedChar = allChars[newIndex]
                            }
                        }
                        else -> append(generatedChar)
                    }
                }
            }
        }
        
        fragmentBinding.pwdGeneratedTextView.text = password
    }
    
    override fun onPause() {
        super.onPause()
        preferenceManager.apply {
            setFloat(PWD_LENGTH, fragmentBinding.pwdLengthSlider.value)
            primarySwitchesList.forEach { switch ->
                primarySwitchesPrefMap[switch]?.let { preferenceKey ->
                    setBoolean(preferenceKey, switch.isChecked)
                }
            }
            setBoolean(PWD_AMB_CHARS, fragmentBinding.avoidAmbCharsSwitch.isChecked)
            setBoolean(PWD_RPT_CHARS, fragmentBinding.avoidAmbCharsSwitch.isChecked)
            setBoolean(PWD_SPACES, fragmentBinding.includeSpacesSwitch.isChecked)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}