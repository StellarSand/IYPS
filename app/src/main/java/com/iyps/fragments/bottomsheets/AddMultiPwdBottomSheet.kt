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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.activities.MultiPwdActivity
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.BottomSheetAddMultiPwdBinding
import com.iyps.databinding.BottomSheetFooterBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.models.MultiPwdItem

class AddMultiPwdBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetAddMultiPwdBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddMultiPwdBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val footerBinding = BottomSheetFooterBinding.bind(bottomSheetBinding.root)
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.isVisible = false
        
        // Edit text
        bottomSheetBinding.multiPwdText.doOnTextChanged { charSequence, _, _, _ ->
            footerBinding.doneBtn.apply {
               if (!isEnabled && charSequence!!.isNotEmpty()) isEnabled = true
            }
        }
        
        // Done
        footerBinding.doneBtn.apply {
            isVisible = true
            setOnClickListener {
                val itemList =
                    bottomSheetBinding.multiPwdText.text!!.split("\n")
                        .filter { it.isNotEmpty() }
                        .map { MultiPwdItem(it) }
                (requireContext().applicationContext as ApplicationManager).multiPasswordsList.addAll(itemList)
                dismiss()
                startActivity(Intent(requireActivity(), MultiPwdActivity::class.java))
            }
        }
        
        // Cancel
        footerBinding.cancelBtn.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}