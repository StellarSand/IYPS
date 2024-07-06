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

package com.iyps.fragments.bottomsheets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.adapters.LicenseItemAdapter
import com.iyps.databinding.BottomSheetFooterBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetLicensesBinding
import com.iyps.models.License

class LicensesBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetLicensesBinding? = null
    private val bottomSheetBinding get() = _binding!!
    private lateinit var licensesList: ArrayList<License>
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetLicensesBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.third_party_licenses)
    
        licensesList = ArrayList<License>().apply {
    
            // zxcvbn4j
            add(License(title = getString(R.string.zxcvbn4j),
                        desc = "${getString(R.string.copyright_zxcvbn4j)}\n\n${getString(R.string.mit_license)}",
                        url = getString(R.string.zxcvbn4j_license_url)))
            
            // SecLists
            add(License(title = getString(R.string.seclists),
                        desc = "${getString(R.string.copyright_seclists)}\n\n${getString(R.string.mit_license)}",
                        url = getString(R.string.seclists_license_url)))
            
            // EFF Diceware
            add(License(title = getString(R.string.eff_diceware),
                        desc = getString(R.string.cc_3_0_license),
                        url = getString(R.string.eff_diceware_license_url)))
            
            // Fastscroll
            add(License(title = getString(R.string.fastscroll),
                        desc = "${getString(R.string.copyright_fastscroll)}\n\n${getString(R.string.apache_2_0_license)}",
                        url = getString(R.string.fastscroll_license_url)))
            
            // Liberapay
            add(License(title = getString(R.string.liberapay_icon),
                        desc = getString(R.string.cc0_1_0_universal_public_domain_license),
                        url = getString(R.string.liberapay_icon_license_url)))
            
            // PayPal
            add(License(title = getString(R.string.paypal_icon),
                        desc = "",
                        url = getString(R.string.paypal_icon_license_url)))
            
            // Ko-fi
            add(License(title = getString(R.string.kofi_icon),
                        desc = "",
                        url = getString(R.string.kofi_icon_license_url)))
            
        }
    
        bottomSheetBinding.licensesRecyclerView.adapter = LicenseItemAdapter(licensesList,
                                                                             requireActivity() as MainActivity)
    
        // Cancel
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).cancelBtn.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}