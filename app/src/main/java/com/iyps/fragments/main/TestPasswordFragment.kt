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

package com.iyps.fragments.main

import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.iyps.activities.MainActivity
import com.iyps.bottomsheets.TestMultiPwdBottomSheet
import com.iyps.fragments.common.BaseTestPasswordFragment
import com.iyps.objects.AppState
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.iyps.utils.ClipboardUtils.Companion.scheduleClipboardClear
import com.iyps.utils.UiUtils.Companion.convertDpToPx
import com.iyps.utils.UiUtils.Companion.showSupportAnimBtmSheet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.getValue

class TestPasswordFragment : BaseTestPasswordFragment() {
    
    private lateinit var mainActivity: MainActivity
    
    override fun setupFragmentContent() {
        mainActivity = requireActivity() as MainActivity
        val prefManager by inject<PreferenceManager>()
        var job: Job? = null
        var isInitialLaunch = true
        val displayMetrics = resources.displayMetrics
        var collapsingToolbarLargeHeightInPx = 0
        var collapsingToolbarTopInsets = -1
        
        TypedValue().let {
            requireContext().theme.resolveAttribute(
                com.google.android.material.R.attr.collapsingToolbarLayoutLargeSize,
                it,
                true
            )
            collapsingToolbarLargeHeightInPx = TypedValue.complexToDimensionPixelSize(it.data, displayMetrics)
        }
        
        // Adjust UI components for edge to edge
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.collapsingToolbar) { _, windowInsets ->
            if (collapsingToolbarTopInsets == -1) {
                val insets =
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    )
                collapsingToolbarTopInsets = insets.top
            }
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.scrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updatePadding(left = insets.left, right = insets.right, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.testMultipleFab) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                                                        or WindowInsetsCompat.Type.displayCutout())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                rightMargin = insets.right + convertDpToPx(requireContext(), 16f)
                bottomMargin = insets.bottom + convertDpToPx(requireContext(), 25f)
            }
            WindowInsetsCompat.CONSUMED
        }
        
        // Set collapsing toolbar to center of screen for first time
        // Don't move this within setOnApplyWindowInsetsListener() above
        setCollapsingToolbarHeight((collapsingToolbarTopInsets + displayMetrics.heightPixels) / 2)
        
        // Prevent dragging of appbar when scrollview is not visible
        val appBarLayoutBehavior =
            AppBarLayout.Behavior().also {
                (fragmentBinding.appBar.layoutParams as CoordinatorLayout.LayoutParams).behavior = it
            }
        appBarLayoutBehavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return !isInitialLaunch
            }
        })
        
        fragmentBinding.scrollView.isVisible = false
        
        fragmentBinding.passwordText.apply {
            if (prefManager.getBoolean(INCOG_KEYBOARD)) {
                imeOptions = IME_FLAG_NO_PERSONALIZED_LEARNING
                inputType = TYPE_TEXT_VARIATION_PASSWORD
            }
            doOnTextChanged { charSequence, _, _, _ ->
                // Introduce a subtle delay
                // So passwords are checked after typing is finished
                job?.cancel()
                job =
                    lifecycleScope.launch {
                        delay(300)
                        if (charSequence!!.isNotEmpty()) {
                            fragmentBinding.copyChipGroup.forEach {
                                (it as? Chip)?.apply {
                                    if (!isEnabled) isEnabled = true
                                }
                            }
                            displayResults(charSequence)
                            if (!isInitialLaunch && AppState.showSupportBtmSheet) {
                                showSupportAnimBtmSheet(parentFragmentManager, prefManager)
                            }
                            if (isInitialLaunch) {
                                isInitialLaunch = false
                                fragmentBinding.appBar.setExpanded(false, true)
                                fragmentBinding.scrollView.isVisible = true
                                setCollapsingToolbarHeight(collapsingToolbarTopInsets + collapsingToolbarLargeHeightInPx)
                            }
                        }
                        // If edit text is empty or cleared, reset everything
                        else {
                            fragmentBinding.copyChipGroup.forEach {
                                (it as? Chip)?.isEnabled = false
                            }
                            resetDetails()
                        }
                    }
            }
            
            // Detect if copied from this app
            customSelectionActionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }
                
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    return true
                }
                
                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        android.R.id.copy -> {
                            copyToClipboard(text.toString())
                            scheduleClipboardClear(requireContext())
                        }
                    }
                    return true
                }
                
                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }
        
        // Fab
        fragmentBinding.testMultipleFab.setOnClickListener {
            TestMultiPwdBottomSheet().show(parentFragmentManager, "TestMultiplePwdBottomSheet")
        }
    }
    
    private fun setCollapsingToolbarHeight(height: Int) {
        fragmentBinding.collapsingToolbar.apply {
            val params = layoutParams
            params.height = height
            layoutParams = params
            requestLayout()
        }
    }
    
    private fun resetDetails() {
        fragmentBinding.apply {
            tenBGuessesStrength.text = naString
            tenKGuessesStrength.text = naString
            tenGuessesStrength.text = naString
            hundredGuessesStrength.text = naString
            tenBGuessesSubtitle.text = naString
            tenKGuessesSubtitle.text = naString
            tenGuessesSubtitle.text = naString
            hundredGuessesSubtitle.text = naString
            warningSubtitle.text = naString
            suggestionsSubtitle.text = naString
            guessesSubtitle.text = naString
            orderMagnSubtitle.text = naString
            orderMagnSubtitle.text = naString
            entropySubtitle.text = naString
            matchSequenceSubtitle.text = naString
            statsSubtitle.text = naString
            
            tenBGuessesStrengthMeter.apply {
                setIndicatorColor(emptyMeterColor)
                setProgressCompat(0, true)
            }
            
            tenKGuessesStrengthMeter.apply {
                setIndicatorColor(emptyMeterColor)
                setProgressCompat(0, true)
            }
            
            tenGuessesStrengthMeter.apply {
                setIndicatorColor(emptyMeterColor)
                setProgressCompat(0, true)
            }
            
            hundredGuessesStrengthMeter.apply {
                setIndicatorColor(emptyMeterColor)
                setProgressCompat(0, true)
            }
        }
        
    }
    
    override fun getCoordinatorLayout(): CoordinatorLayout {
        return mainActivity.activityBinding.mainCoordLayout
    }
    
    override fun getSnackbarAnchorView(): View {
        return fragmentBinding.testMultipleFab
    }
}