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
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetLicensesBinding
import com.iyps.utils.IntentUtils.Companion.openURL

class LicensesBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetLicensesBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
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
        
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
    
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
        bottomSheetBinding.cancelButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}