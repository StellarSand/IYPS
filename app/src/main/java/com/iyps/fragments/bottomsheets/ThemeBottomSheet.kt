package com.iyps.fragments.bottomsheets

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.R
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetThemeBinding
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.THEME_PREF

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
        
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)
        val preferenceManager = PreferenceManager(requireContext())
        
        headerBinding.bottomSheetTitle.setText(R.string.theme)
        
        // Show "follow system" option only on SDK 29 and above
        bottomSheetBinding.followSystem.isVisible = Build.VERSION.SDK_INT >= 29
        
        // Radio group
        bottomSheetBinding.themeChipGroup.apply {
        
            // Default checked chip
            if (preferenceManager.getInt(THEME_PREF) == 0) {
                if (Build.VERSION.SDK_INT >= 29) {
                    preferenceManager.setInt(THEME_PREF, R.id.follow_system)
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
                when (checkedChip) {
                    R.id.follow_system ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                
                    R.id.light ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                
                    R.id.dark ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                dismiss()
            
            }
        }
    
        bottomSheetBinding.cancelButton.setOnClickListener { dismiss() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}