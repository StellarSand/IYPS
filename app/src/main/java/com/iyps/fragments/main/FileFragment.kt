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

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.iyps.activities.DetailsActivity
import com.iyps.activities.MainActivity
import com.iyps.activities.MainActivity.Companion.READ_FILE_REQ_CODE
import com.iyps.adapters.FileItemAdapter
import com.iyps.databinding.FragmentFileBinding
import com.iyps.models.FileItem
import java.io.*

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

        // Select file button
        val mainActivityBinding = (requireActivity() as MainActivity).activityBinding
        mainActivityBinding.selectButton.isVisible = true
        mainActivityBinding.selectButton.setOnClickListener {

            // If not granted ask for permission
            if (ContextCompat.checkSelfPermission(requireActivity(), READ_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(READ_EXTERNAL_STORAGE), READ_FILE_REQ_CODE)
            }
            else {
                var intent = Intent()
                            .setType("text/plain")
                            .setAction(Intent.ACTION_GET_CONTENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)

                intent = Intent.createChooser(intent, "Select file")
                filePicker.launch(intent)
            }
        }

    }

    private var filePicker = registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {

            val data = result.data!!
            val fileUri = data.data

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
                            val fileItem = FileItem()
                            fileItem.passwordLine = line
                            fileItemList.add(fileItem)
                        }

                    }

                }
                inputStream!!.close()
                bufferedReader.close()
            }
            catch (fileNotFoundException: FileNotFoundException) {
                fileNotFoundException.printStackTrace()
                Toast.makeText(requireContext(), "File not found", Toast.LENGTH_SHORT).show()
            }
            catch (ioException: IOException) {
                ioException.printStackTrace()
                Toast.makeText(requireContext(), "Error reading file", Toast.LENGTH_SHORT).show()
            }

            fragmentBinding.recyclerView.adapter = fileItemAdapter
            fragmentBinding.textView.visibility = View.GONE

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