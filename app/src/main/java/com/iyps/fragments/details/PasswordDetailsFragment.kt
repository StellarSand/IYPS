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

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.fragments.common.BasePwdResultsFragment

class PasswordDetailsFragment : BasePwdResultsFragment() {
    
    private lateinit var detailsActivity: DetailsActivity
    
    override fun setupFragmentContent() {
        detailsActivity = (requireActivity() as DetailsActivity)
        val password = detailsActivity.passwordLine
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        
        fragmentBinding.apply {
            testMultipleFab.isVisible = false
            passwordBox.hint = getString(R.string.password)
            passwordText.apply {
                setText(password)
                isFocusable = false
                isCursorVisible = false
            }
        }
        
        displayPwdResults(password)
    }
    
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return detailsActivity.activityBinding.detailsCoordLayout
    }
    
    override fun getSnackbarAnchorView(): View {
        return detailsActivity.activityBinding.detailsDockedToolbar
    }
}
