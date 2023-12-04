/*
 * Copyright (c) 2022-present StellarSand
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

package com.iyps.fragments.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.appmanager.ApplicationManager
import com.iyps.common.EvaluatePassword
import com.iyps.databinding.FragmentPasswordBinding
import com.iyps.utils.ResultUtils

class DetailsFragment : Fragment() {
    
    private var _binding: FragmentPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var passwordString: String
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val zxcvbn = (requireContext().applicationContext as ApplicationManager).zxcvbn
        passwordString = (requireActivity() as DetailsActivity).passwordLine
        fragmentBinding.lengthSubtitle.text = "\u2022 ${getString(R.string.length)}"
        fragmentBinding.uppercaseSubtitle.text = "\u2022 ${getString(R.string.uppercase)}"
        fragmentBinding.lowercaseSubtitle.text = "\u2022 ${getString(R.string.lowercase)}"
        fragmentBinding.numbersSubtitle.text = "\u2022 ${getString(R.string.numbers)}"
        fragmentBinding.specialCharsSubtitle.text = "\u2022 ${getString(R.string.special_char)}"
        fragmentBinding.spacesSubtitle.text = "\u2022 ${getString(R.string.spaces)}"
        
        /*########################################################################################*/
        
        fragmentBinding.passwordText.apply {
            setText(passwordString)
            isFocusable = false
            isCursorVisible = false
        }
        
        EvaluatePassword(zxcvbn = zxcvbn,
                         password = passwordString,
                         fragmentPasswordBinding = fragmentBinding,
                         context = requireContext(),
                         resultUtils = ResultUtils(requireContext()))
    }
    
    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}