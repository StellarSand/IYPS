/*
 * Copyright (c) 2022 the-weird-aquarian
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

package com.iyps.fragments.help

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.databinding.BottomSheetAuthorsBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetLicensesBinding
import com.iyps.databinding.BottomSheetThemeBinding
import com.iyps.databinding.FragmentAboutBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.utils.IntentUtils.Companion.openURL

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var version: String

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        preferenceManager = PreferenceManager(requireContext())

        // Version
        try {
            version = ("${getString(R.string.app_version)} ${
                        requireContext().packageManager
                            .getPackageInfo(requireContext().packageName, 0)
                            .versionName}")
        }
        catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        fragmentBinding.version.text = version
    
        // Theme
        fragmentBinding.theme
            .setOnClickListener { themeBottomSheet() }

        // Authors
        fragmentBinding.authors
            .setOnClickListener { authorsBottomSheet() }

        // Contributors
        fragmentBinding.contributors
            .setOnClickListener {
                openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/graphs/contributors")
            }
    
        // Report an issue
        fragmentBinding.reportIssue.setOnClickListener {
            openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/issues")
        }

        // Privacy policy
        fragmentBinding.privacyPolicy
            .setOnClickListener {
                openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/PRIVACY.md")
            }

        // Licenses
        fragmentBinding.licenses
            .setOnClickListener { licensesBottomSheet() }

        // View on GitHub
        fragmentBinding.viewOnGit
            .setOnClickListener {
                openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS")
            }

    }
    
    private fun themeBottomSheet() {
        
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetThemeBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        
        // Title
        headerBinding.bottomSheetTitle.setText(R.string.theme)
        
        // Show system default option only on SDK 29 and above
        if (Build.VERSION.SDK_INT >= 29) {
            bottomSheetBinding.optionDefault.visibility = View.VISIBLE
        }
        else {
            bottomSheetBinding.optionDefault.visibility = View.GONE
        }
        
        // Default checked radio
        if (preferenceManager.getInt(PreferenceManager.THEME_PREF) == 0) {
            
            if (Build.VERSION.SDK_INT >= 29) {
                preferenceManager.setInt(PreferenceManager.THEME_PREF, R.id.option_default)
            }
            else {
                preferenceManager.setInt(PreferenceManager.THEME_PREF, R.id.option_light)
            }
            
        }
        bottomSheetBinding.optionsRadiogroup.check(preferenceManager.getInt(PreferenceManager.THEME_PREF))
        
        bottomSheetBinding.optionsRadiogroup
            .setOnCheckedChangeListener { _, checkedId ->
                
                when(checkedId) {
                    
                    R.id.option_default -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    R.id.option_light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    R.id.option_dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    
                }
                
                preferenceManager.setInt(PreferenceManager.THEME_PREF, checkedId)
                bottomSheetDialog.dismiss()
                requireActivity().recreate()
            }
        
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { bottomSheetDialog.cancel() }
        bottomSheetDialog.show()
    }

    private fun authorsBottomSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetAuthorsBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Title
        headerBinding.bottomSheetTitle.text = getString(R.string.authors)

        // Author 1
        bottomSheetBinding.author1.setOnClickListener {
            openURL(requireActivity(), "https://github.com/the-weird-aquarian")
            bottomSheetDialog.dismiss()
        }

        // Author 2
        bottomSheetBinding.author2.setOnClickListener {
            openURL(requireActivity(), "https://github.com/parveshnarwal")
            bottomSheetDialog.dismiss()
        }

        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { bottomSheetDialog.dismiss() }

        bottomSheetDialog.show()

    }
    
    private fun licensesBottomSheet() {
        
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetLicensesBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        
        // Title
        headerBinding.bottomSheetTitle.setText(R.string.licenses)
    
        // IYPS
        bottomSheetBinding.iyps.setOnClickListener {
            openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS")
        }
    
        // zxcvbn4j
        bottomSheetBinding.zxcvbn4j.setOnClickListener {
            openURL(requireActivity(), "https://github.com/nulab/zxcvbn4j")
        }
    
        // Material Design Icons
        bottomSheetBinding.mdIcons.setOnClickListener {
            openURL(requireActivity(), "https://github.com/Templarian/MaterialDesign")
        }
        
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { bottomSheetDialog.cancel() }
        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}