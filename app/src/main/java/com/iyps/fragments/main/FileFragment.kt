/*
 * Copyright (c) 2022 the-weird-aquarian
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

package com.iyps.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.iyps.activities.DetailsActivity
import com.iyps.adapters.FileItemAdapter
import com.iyps.databinding.FragmentFileBinding
import com.iyps.models.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader

class FileFragment : Fragment(), FileItemAdapter.OnItemClickListener {
    
    private var _binding: FragmentFileBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var fileItemList: ArrayList<FileItem>
    private lateinit var fileItemAdapter: FileItemAdapter
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentFileBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        fileItemList = ArrayList()
        fileItemAdapter = FileItemAdapter(fileItemList, this)
        
        /*########################################################################################*/
        
        // Select file FAB
        fragmentBinding.selectFab.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                }
            
            filePicker.launch(intent)
        }
        
    }
    
    private var filePicker = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
        
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data!!
            val fileUri = data.data
            
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = requireActivity().contentResolver.openInputStream(fileUri!!)
                    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    var lineList: List<String>
                    
                    if (fileItemList.isNotEmpty()) {
                        fileItemList.clear()
                    }
                    
                    // Read file line by line
                    while (bufferedReader.readLine().also { lineList = listOf(it) } != null) {
                        for (line in lineList) {
                            if (line.isNotEmpty()) {
                                fileItemList.add(FileItem(passwordLine = line))
                            }
                        }
                    }
                    inputStream!!.close()
                    bufferedReader.close()
                }
                catch (fileNotFoundException: FileNotFoundException) {
                    fileNotFoundException.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
                    }
                }
                catch (ioException: IOException) {
                    ioException.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error reading file", Toast.LENGTH_SHORT).show()
                    }
                }
                
                withContext(Dispatchers.Main) {
                    fragmentBinding.recyclerView.apply {
                        adapter = fileItemAdapter
                        addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                                super.onScrollStateChanged(recyclerView, newState)
                                    fragmentBinding.selectFab.apply {
                                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                            postDelayed({ extend() }, 500)
                                        }
                                        else if (recyclerView.canScrollVertically(-1)
                                                 || recyclerView.canScrollVertically(1)) {
                                            shrink()
                                        }
                                    }
                            }
                        })
                    }
                    fragmentBinding.textView.isVisible = false
                }
            }
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        val fileItem = fileItemList[position]
        startActivity(Intent(requireActivity(), DetailsActivity::class.java)
                          .putExtra("PwdLine", fileItem.passwordLine))
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}