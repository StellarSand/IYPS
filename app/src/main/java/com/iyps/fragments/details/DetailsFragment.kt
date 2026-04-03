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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.common.evaluatePassword
import com.iyps.common.getFormattedResultsText
import com.iyps.databinding.FragmentTestPasswordBinding
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.FormatUtils.Companion.generateNewFilename
import com.iyps.utils.IntentUtils.Companion.shareText
import com.iyps.utils.ResultUtils
import com.iyps.utils.UiUtils.Companion.showSnackbar
import com.nulabinc.zxcvbn.Zxcvbn
import org.koin.android.ext.android.get

class DetailsFragment : Fragment() {
    
    private var _binding: FragmentTestPasswordBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var detailsActivity: DetailsActivity
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentTestPasswordBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        detailsActivity = (requireActivity() as DetailsActivity)
        val password = detailsActivity.passwordLine
        val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
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
                isSingleLine = true
            }
        }
        
        fragmentBinding.evaluatePassword(
            zxcvbn = get<Zxcvbn>(),
            password = password,
            context = requireContext(),
            resultUtils = ResultUtils(requireContext())
        )
        
        // Copy
        fragmentBinding.copyChip.setOnClickListener {
            val clipData = ClipData.newPlainText("IYPS", fragmentBinding.getFormattedResultsText(requireContext()))
            clipData.hideSensitiveContent()
            clipboardManager.setPrimaryClip(clipData)
            // Show snackbar only if 12L or lower to avoid duplicate notifications
            // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
            if (Build.VERSION.SDK_INT <= 32) {
                showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                             requireContext().getString(R.string.copied_to_clipboard),
                             detailsActivity.activityBinding.detailsDockedToolbar)
            }
        }
        
        // Share
        fragmentBinding.shareChip.setOnClickListener {
            requireActivity().shareText(fragmentBinding.getFormattedResultsText(requireContext()))
        }
        
        // Export
        fragmentBinding.exportChip.setOnClickListener {
            exportToFilePicker.launch(generateNewFilename())
        }
    }
    
    private val exportToFilePicker =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) { uri ->
            uri?.let {
                try {
                    requireContext().contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(fragmentBinding.getFormattedResultsText(requireContext()).toByteArray())
                    }
                    showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                                 getString(R.string.export_success),
                                 detailsActivity.activityBinding.detailsDockedToolbar)
                }
                catch (_: Exception) {
                    showSnackbar(detailsActivity.activityBinding.detailsCoordLayout,
                                 getString(R.string.export_fail),
                                 detailsActivity.activityBinding.detailsDockedToolbar)
                }
            }
        }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}