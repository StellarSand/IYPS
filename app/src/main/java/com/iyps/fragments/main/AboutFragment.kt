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

package com.iyps.fragments.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.iyps.BuildConfig
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.databinding.FragmentAboutBinding
import com.iyps.fragments.bottomsheets.AuthorsBottomSheet
import com.iyps.fragments.bottomsheets.LicensesBottomSheet
import com.iyps.fragments.bottomsheets.ThemeBottomSheet
import com.iyps.utils.IntentUtils.Companion.openURL

class AboutFragment : Fragment() {
    
    private var _binding: FragmentAboutBinding? = null
    private val fragmentBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        (requireActivity() as MainActivity).activityBinding.selectButton.isVisible = false
        
        // Version
        @SuppressLint("SetTextI18n")
        fragmentBinding.version.text = "${getString(R.string.app_version)}: ${BuildConfig.VERSION_NAME}"
        
        // Theme
        fragmentBinding.theme
            .setOnClickListener {
                ThemeBottomSheet().show(parentFragmentManager, "ThemeBottomSheet")
            }
        
        // Authors
        fragmentBinding.authors
            .setOnClickListener {
                AuthorsBottomSheet().show(parentFragmentManager, "AuthorsBottomSheet")
            }
        
        // Contributors
        fragmentBinding.contributors
            .setOnClickListener {
                openURL(requireActivity(), getString(R.string.iyps_contributors_url))
            }
        
        // Report an issue
        fragmentBinding.reportIssue.setOnClickListener {
            openURL(requireActivity(), getString(R.string.iyps_issues_url))
        }
        
        // Privacy policy
        fragmentBinding.privacyPolicy
            .setOnClickListener {
                openURL(requireActivity(), getString(R.string.iyps_privacy_policy_url))
            }
        
        // Licenses
        fragmentBinding.licenses
            .setOnClickListener {
                LicensesBottomSheet().show(parentFragmentManager, "LicensesBottomSheet")
            }
        
        // View on GitHub
        fragmentBinding.viewOnGit
            .setOnClickListener {
                openURL(requireActivity(), getString(R.string.iyps_github_url))
            }
        
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}