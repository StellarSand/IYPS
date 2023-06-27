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

package com.iyps.fragments.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.R
import com.iyps.databinding.BottomSheetAboutBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.utils.IntentUtils.Companion.openURL

class AboutBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetAboutBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = BottomSheetAboutBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
    
        // Title
        headerBinding.bottomSheetTitle.setText(R.string.about)
        
        // Privacy policy
        bottomSheetBinding.privacyPolicy
            .setOnClickListener {
                openURL(requireActivity(), getString(R.string.iyps_privacy_policy_url))
            }
        
        // Licenses
        bottomSheetBinding.licenses
            .setOnClickListener {
                LicensesBottomSheet().show(parentFragmentManager, "LicensesBottomSheet")
            }
    
        // Contributors
        bottomSheetBinding.contributors
            .setOnClickListener {
                openURL(requireActivity(), getString(R.string.iyps_contributors_url))
            }
    
        // Report an issue
        bottomSheetBinding.reportIssue.setOnClickListener {
            openURL(requireActivity(), getString(R.string.iyps_issues_url))
        }
        
        // View on GitHub
        bottomSheetBinding.viewOnGit
            .setOnClickListener {
                openURL(requireActivity(), getString(R.string.iyps_github_url))
            }
    
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { dismiss() }
        
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
}