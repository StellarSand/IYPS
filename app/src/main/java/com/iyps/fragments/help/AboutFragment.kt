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

package com.iyps.fragments.help

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.iyps.R
import com.iyps.databinding.BottomSheetAuthorsBinding
import com.iyps.databinding.BottomSheetHeaderBinding
import com.iyps.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val fragmentBinding get() = _binding!!
    private lateinit var version: String

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Version
        try {
            version = ("${getString(R.string.app_version)} ${
                        requireContext().packageManager
                            .getPackageInfo(requireContext().packageName, 0)
                            .versionName}")
        }
        catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        fragmentBinding.version.text = version

        // Authors
        fragmentBinding.authors
            .setOnClickListener { authorsBottomSheet() }

        // Contributors
        fragmentBinding.contributors
            .setOnClickListener {
                openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/graphs/contributors")
            }

        // Privacy policy
        fragmentBinding.privacyPolicy
            .setOnClickListener {
                openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS/blob/master/PRIVACY.md")
            }

        // Licenses
        fragmentBinding.licenses
            .setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_from_end, R.anim.slide_to_start,
                                         R.anim.slide_from_start, R.anim.slide_to_end)
                    .replace(R.id.activity_host_fragment, LicensesFragment())
                    .addToBackStack(null)
                    .commit()
            }

        // View on GitHub
        fragmentBinding.viewOnGit
            .setOnClickListener {
                openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS")
            }

    }

    private fun authorsBottomSheet() {

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetBinding = BottomSheetAuthorsBinding.inflate(layoutInflater)
        val headerBinding = BottomSheetHeaderBinding.bind(bottomSheetBinding.root)

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        // Title
        headerBinding.bottomSheetTitle.text = getString(R.string.authors)

        // Author 1
        bottomSheetBinding.author1.setOnClickListener {
            openURL(requireActivity(), "https://github.com/the-weird-aquarian")
            bottomSheetDialog.dismiss()
        }

        // Author 2
        bottomSheetBinding.author2.setOnClickListener {
            openURL(requireActivity(), "https://github.com/parveshnarwal")
            bottomSheetDialog.dismiss()
        }

        // Cancel
        bottomSheetBinding.cancelButton.setOnClickListener { bottomSheetDialog.dismiss() }

        bottomSheetDialog.show()

    }

    companion object {
        fun openURL(activityFrom: Activity, URL: String?) {

            try {
                activityFrom.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL)))
            }
            // If browsers not installed, show toast
            catch (e: ActivityNotFoundException) {
                Toast.makeText(activityFrom, activityFrom.resources.getString(R.string.no_browsers), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}