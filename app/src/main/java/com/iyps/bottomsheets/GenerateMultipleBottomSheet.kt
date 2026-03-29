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

package com.iyps.bottomsheets

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
import com.iyps.adapters.GenerateMultiAdapter
import com.iyps.databinding.BottomSheetFooterBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetRecyclerViewBinding
import com.iyps.models.MultiPwdItem
import com.iyps.objects.GenerateMultiPwdList

class GenerateMultipleBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetRecyclerViewBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetRecyclerViewBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.generate_multiple)
        
        bottomSheetBinding.licensesRecyclerView.adapter =
            GenerateMultiAdapter(
                GenerateMultiPwdList.pwdList as ArrayList<MultiPwdItem>,
                requireActivity() as MainActivity
            )
        
        // Cancel
        footerBinding.cancelBtn.setOnClickListener { dismiss() }
        
        // Regenerate
        /*footerBinding.doneBtn.apply {
            text = getString(R.string.regenerate)
            visibility = View.VISIBLE
            isEnabled = true
        }*/
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        GenerateMultiPwdList.pwdList.clear()
        _binding = null
    }
}