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

package com.iyps.bottomsheets

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iyps.R
import com.iyps.activities.MultiPwdActivity
import com.iyps.databinding.BottomSheetFooterBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.BottomSheetTestMultiPwdBinding
import com.iyps.objects.MultiPwdList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class TestMultiPwdBottomSheet : BottomSheetDialogFragment() {
    
    private var _binding: BottomSheetTestMultiPwdBinding? = null
    private val bottomSheetBinding get() = _binding!!
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = BottomSheetTestMultiPwdBinding.inflate(inflater, container, false)
        return bottomSheetBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        // Title
        BottomSheetHeaderBinding.bind(bottomSheetBinding.root).bottomSheetTitle.text = getString(R.string.test_multi_pwds)
        
        // Add
        bottomSheetBinding.addMultiple.setOnClickListener {
            dismiss()
            AddMultiPwdBottomSheet().show(parentFragmentManager, "AddMultiplePwdBottomSheet")
        }
        
        // Select file
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
        bottomSheetBinding.selectFile.setOnClickListener {
            filePicker.launch(intent)
        }
        
        // Cancel
        BottomSheetFooterBinding.bind(bottomSheetBinding.root).cancelBtn.setOnClickListener { dismiss() }
    }
    
    private var filePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val fileUri = result.data?.data
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        fileUri?.let {
                            requireActivity().contentResolver.openInputStream(it)?.use { inputStream ->
                                inputStream.bufferedReader().use { bufferedReader ->
                                    MultiPwdList.pwdList.apply {
                                        if (isNotEmpty()) clear()
                                        // Read file line by line
                                        bufferedReader.forEachLine { line ->
                                            if (line.isNotEmpty()) add(line)
                                        }
                                    }
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            dismiss()
                            startActivity(
                                Intent(requireActivity(), MultiPwdActivity::class.java),
                                ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle()
                            )
                        }
                    }
                    catch (fileNotFoundException: FileNotFoundException) {
                        fileNotFoundException.printStackTrace()
                        withContext(Dispatchers.Main) {
                            /*UiUtils.showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                                 "File not found",
                                                 fragmentBinding.selectFab)*/
                        }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            /*UiUtils.showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                                 "Error reading file",
                                                 fragmentBinding.selectFab)*/
                        }
                    }
                }
            }
        }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}