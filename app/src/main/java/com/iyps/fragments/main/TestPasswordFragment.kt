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

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.TypedValue
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.inputmethod.EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.common.EvaluatePassword
import com.iyps.databinding.FragmentTestPasswordBinding
import com.iyps.bottomsheets.TestMultiPwdBottomSheet
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.INCOG_KEYBOARD
import com.iyps.utils.ClipboardUtils.Companion.clearClipboard
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.ClipboardUtils.Companion.manageClipboard
import com.iyps.utils.UiUtils.Companion.showSnackbar
import com.iyps.utils.ResultUtils
import com.iyps.utils.UiUtils.Companion.convertDpToPx
import com.nulabinc.zxcvbn.Zxcvbn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class TestPasswordFragment : Fragment() {
    
    private var _binding: FragmentTestPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var mainActivity: MainActivity
    private lateinit var naString: String
    private var emptyMeterColor = 0
    private lateinit var clipboardManager: ClipboardManager
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        mainActivity = requireActivity() as MainActivity
        var job: Job? = null
        val resultUtils = ResultUtils(requireContext())
        var isInitialLaunch = true
        clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        naString = getString(R.string.na)
        emptyMeterColor = resources.getColor(android.R.color.transparent, requireContext().theme)
        
        // Adjust UI components for edge to edge
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
        
        // Set collapsing toolbar height
        ViewCompat.setOnApplyWindowInsetsListener(fragmentBinding.collapsingToolbar) { view, windowInsets ->
            val insets =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                )
            
            val heightInPx =
                if (isInitialLaunch) {
                    val usableHeight =
                        if (Build.VERSION.SDK_INT >= 30)
                            requireContext().getSystemService<WindowManager>()!!.currentWindowMetrics.bounds.height()
                        else
                            Resources.getSystem().displayMetrics.heightPixels
                    
                    (insets.top + usableHeight) / 2 // Center of screen
                }
                else {
                    // ?attr/collapsingToolbarLayoutLargeSize
                    val typedValue = TypedValue()
                    requireContext().theme.resolveAttribute(
                        com.google.android.material.R.attr.collapsingToolbarLayoutLargeSize,
                        typedValue,
                        true
                    )
                    insets.top + TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
                }
            
            view.apply {
                val params = layoutParams
                params.height = heightInPx
                layoutParams = params
                requestLayout()
            }
            
            WindowInsetsCompat.CONSUMED
        }
        
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
            if (get<PreferenceManager>().getBoolean(INCOG_KEYBOARD)) {
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
                            EvaluatePassword(zxcvbn = get<Zxcvbn>(),
                                             password = charSequence,
                                             fragmentBinding = fragmentBinding,
                                             context = requireContext(),
                                             resultUtils = resultUtils)
                            if (isInitialLaunch) {
                                isInitialLaunch = false
                                fragmentBinding.appBar.setExpanded(false, true)
                                fragmentBinding.scrollView.isVisible = true
                            }
                        }
                        // If edit text is empty or cleared, reset everything
                        else {
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
                            val clipData = ClipData.newPlainText("IYPS", text)
                            clipData.hideSensitiveContent()
                            clipboardManager.setPrimaryClip(clipData)
                            // Only show snackbar in 12L or lower to avoid duplicate notifications
                            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
                            if (Build.VERSION.SDK_INT <= 32) {
                                showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                             requireContext().getString(R.string.copied_to_clipboard),
                                             fragmentBinding.testMultipleFab)
                            }
                        }
                    }
                    return true
                }
                
                override fun onDestroyActionMode(mode: ActionMode?) {}
            }
        }
        
        // Clipboard
        manageClipboard(clipboardManager, lifecycleScope)
        
        // Fab
        fragmentBinding.testMultipleFab.setOnClickListener {
            TestMultiPwdBottomSheet().show(parentFragmentManager, "TestMultiplePwdBottomSheet")
        }
    }
    
    // Reset details
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
    
    // Clear clipboard immediately when fragment destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        clipboardManager.apply {
            if (hasPrimaryClip() && primaryClipDescription?.label == "IYPS") clearClipboard()
        }
        _binding = null
    }
}