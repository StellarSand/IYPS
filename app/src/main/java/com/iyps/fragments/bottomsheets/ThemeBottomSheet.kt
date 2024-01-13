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

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.R
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.BottomSheetFooterBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetThemeBinding
import com.iyps.preferences.PreferenceManager.Companion.THEME_PREF
import com.iyps.utils.UiUtils.Companion.setAppTheme

class ThemeBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetThemeBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        
        _binding = BottomSheetThemeBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        val preferenceManager = (requireContext().applicationContext as ApplicationManager).preferenceManager
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.theme)
        
        // Show "follow system" option only on SDK 29 and above
        bottomSheetBinding.followSystem.isVisible = Build.VERSION.SDK_INT >= 29
        
        // Radio group
        bottomSheetBinding.themeChipGroup.apply {
        
            // Default checked chip
            if (preferenceManager.getInt(THEME_PREF) == 0) {
                if (Build.VERSION.SDK_INT >= 29) {
                    preferenceManager.setInt(THEME_PREF, R.id.followSystem)
                }
                else {
                    preferenceManager.setInt(THEME_PREF, R.id.light)
                }
            }
            check(preferenceManager.getInt(THEME_PREF))
        
            // On selecting option
            setOnCheckedStateChangeListener { _, checkedIds ->
                val checkedChip = checkedIds.first()
                preferenceManager.setInt(THEME_PREF, checkedChip)
                setAppTheme(checkedChip)
                dismiss()
            
            }
        }
        
        // Cancel
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).cancelBtn.setOnClickListener { dismiss() }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}