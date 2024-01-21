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

package com.iyps.fragments.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.appmanager.ApplicationManager
import com.iyps.common.EvaluatePassword
import com.iyps.databinding.FragmentTestPasswordBinding
import com.iyps.utils.ResultUtils

class DetailsFragment : Fragment() {
    
    private var _binding: FragmentTestPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var passwordString: String
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val zxcvbn = (requireContext().applicationContext as ApplicationManager).zxcvbn
        passwordString = (requireActivity() as DetailsActivity).passwordLine
        
        fragmentBinding.apply {
            lengthSubtitle.text = "\u2022 ${getString(R.string.length)}"
            uppercaseSubtitle.text = "\u2022 ${getString(R.string.uppercase)}"
            lowercaseSubtitle.text = "\u2022 ${getString(R.string.lowercase)}"
            numbersSubtitle.text = "\u2022 ${getString(R.string.numbers)}"
            specialCharsSubtitle.text = "\u2022 ${getString(R.string.special_char)}"
            spacesSubtitle.text = "\u2022 ${getString(R.string.spaces)}"
            testMultipleFab.isVisible = false
            passwordText.apply {
                setText(passwordString)
                isFocusable = false
                isCursorVisible = false
            }
        }
        
        EvaluatePassword(zxcvbn = zxcvbn,
                         password = passwordString,
                         fragmentBinding = fragmentBinding,
                         context = requireContext(),
                         resultUtils = ResultUtils(requireContext()))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}