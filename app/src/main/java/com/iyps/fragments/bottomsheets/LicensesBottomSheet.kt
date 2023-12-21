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
    private lateinit var licenseList: ArrayList<License>
    
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
    
        licenseList = ArrayList<License>().apply {
    
            // zxcvbn4j
            add(License(getString(R.string.zxcvbn4j),
                        "${getString(R.string.copyright_zxcvbn4j)}\n\n${getString(R.string.mit_license)}",
                        getString(R.string.zxcvbn4j_license_url)))
            
            // SecLists
            add(License(getString(R.string.seclists),
                        "${getString(R.string.copyright_seclists)}\n\n${getString(R.string.mit_license)}",
                        getString(R.string.seclists_license_url)))
            
            // Fastscroll
            add(License(getString(R.string.fastscroll),
                        "${getString(R.string.copyright_fastscroll)}\n\n${getString(R.string.apache_2_0_license)}",
                        getString(R.string.fastscroll_license_url)))
            
        }
    
        bottomSheetBinding.licensesRecyclerView.adapter = LicenseItemAdapter(licenseList,
                                                                             requireActivity() as MainActivity)
    
        // Cancel
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).cancelButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}