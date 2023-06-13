package com.iyps.fragments.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.R
import com.iyps.databinding.BottomSheetAuthorsBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.utils.IntentUtils.Companion.openURL

class AuthorsBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetAuthorsBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetAuthorsBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
    
        // Title
        headerBinding.bottomSheetTitle.text = getString(R.string.authors)
    
        // Author 1
        bottomSheetBinding.author1.setOnClickListener {
            openURL(requireActivity(), "https://github.com/the-weird-aquarian")
            dismiss()
        }
    
        // Author 2
        bottomSheetBinding.author2.setOnClickListener {
            openURL(requireActivity(), "https://github.com/parveshnarwal")
            dismiss()
        }
    
        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}