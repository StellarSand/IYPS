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

import android.text.InputFilter
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.fragments.common.BaseTestPasswordFragment

class PassphraseDetailsFragment : BaseTestPasswordFragment() {
    
    private lateinit var detailsActivity: DetailsActivity
    
    override fun setupFragmentContent() {
        isPassphrase = true
        detailsActivity = (requireActivity() as DetailsActivity)
        val passphrase = detailsActivity.passwordLine
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        
        fragmentBinding.apply {
            crackTimeTitle.isVisible = false
            tenBGuessesStrengthMeter.isVisible = false
            tenBGuessesTitle.isVisible = false
            tenBGuessesSubtitle.isVisible = false
            tenBGuessesStrengthTitle.isVisible = false
            tenBGuessesStrength.isVisible = false
            tenKGuessesStrengthMeter.isVisible = false
            tenKGuessesTitle.isVisible = false
            tenKGuessesSubtitle.isVisible = false
            tenKGuessesStrengthTitle.isVisible = false
            tenKGuessesStrength.isVisible = false
            tenGuessesStrengthMeter.isVisible = false
            tenGuessesTitle.isVisible = false
            tenGuessesSubtitle.isVisible = false
            tenGuessesStrengthTitle.isVisible = false
            tenGuessesStrength.isVisible = false
            hundredGuessesStrengthMeter.isVisible = false
            hundredGuessesTitle.isVisible = false
            hundredGuessesSubtitle.isVisible = false
            hundredGuessesStrengthTitle.isVisible = false
            hundredGuessesStrength.isVisible = false
            warningTitle.isVisible = false
            warningSubtitle.isVisible = false
            suggestionsTitle.isVisible = false
            suggestionsSubtitle.isVisible = false
            guessesTitle.isVisible = false
            guessesSubtitle.isVisible = false
            orderMagnTitle.isVisible = false
            orderMagnSubtitle.isVisible = false
            matchSequenceTitle.isVisible = false
            matchSequenceSubtitle.isVisible = false
            testMultipleFab.isVisible = false
            passwordBox.hint = getString(R.string.passphrase)
            passwordText.apply {
                // Remove 128 chars limit
                filters = filters.filterNot { it is InputFilter.LengthFilter }.toTypedArray()
                
                setText(passphrase)
                isFocusable = false
                isCursorVisible = false
                isSingleLine = true
            }
        }
        
        displayPhraseResults(passphrase)
    }
    
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return detailsActivity.activityBinding.detailsCoordLayout
    }
    
    override fun getSnackbarAnchorView(): View {
        return detailsActivity.activityBinding.detailsDockedToolbar
    }
}