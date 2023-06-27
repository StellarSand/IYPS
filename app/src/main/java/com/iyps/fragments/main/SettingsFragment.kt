/*
 * Copyright (c) 2022-present the-weird-aquarian
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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.iyps.BuildConfig
import com.iyps.R
import com.iyps.databinding.FragmentSettingsBinding
import com.iyps.fragments.bottomsheets.AboutBottomSheet
import com.iyps.fragments.bottomsheets.ThemeBottomSheet
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.BLOCK_SS
import com.iyps.preferences.PreferenceManager.Companion.INCOG_KEYBOARD

class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val preferenceManager = PreferenceManager(requireContext())
        
        // Version
        @SuppressLint("SetTextI18n")
        fragmentBinding.version.text = "${getString(R.string.app_version)}: ${BuildConfig.VERSION_NAME}"
        
        // Theme
        fragmentBinding.theme
            .setOnClickListener {
                ThemeBottomSheet().show(parentFragmentManager, "ThemeBottomSheet")
            }
        
        // Block screenshots
        fragmentBinding.blockScreenshotsSwitch.apply {
            isChecked = preferenceManager.getBoolean(BLOCK_SS)
            setOnCheckedChangeListener { _, isChecked ->
                preferenceManager.setBoolean(BLOCK_SS, isChecked)
                when (isChecked) {
                    true -> requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                                                              WindowManager.LayoutParams.FLAG_SECURE)
                    false -> requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }
        
        // Incognito keyboard
        fragmentBinding.incognitoKeyboardSwitch.apply {
            isChecked = preferenceManager.getBoolean(INCOG_KEYBOARD)
            setOnCheckedChangeListener { _, isChecked ->
                preferenceManager.setBoolean(INCOG_KEYBOARD, isChecked)
            }
        }
        
        // About
        fragmentBinding.about
            .setOnClickListener {
                AboutBottomSheet().show(parentFragmentManager, "AboutBottomSheet")
            }
        
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}