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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.activities.HelpActivity
import com.iyps.databinding.FragmentLicensesBinding
import com.iyps.utils.IntentUtils.Companion.openURL

class LicensesFragment : Fragment() {

    private var _binding: FragmentLicensesBinding? = null
    private val fragmentBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        _binding = FragmentLicensesBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (requireActivity() as HelpActivity).supportActionBar?.title = getString(R.string.licenses)

        // IYPS
        fragmentBinding.iyps.setOnClickListener {
            openURL(requireActivity(), "https://github.com/the-weird-aquarian/IYPS")
        }

        // zxcvbn4j
        fragmentBinding.zxcvbn4j.setOnClickListener {
            openURL(requireActivity(), "https://github.com/nulab/zxcvbn4j")
        }

        // Material Design Icons
        fragmentBinding.mdIcons.setOnClickListener {
            openURL(requireActivity(), "https://github.com/Templarian/MaterialDesign")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as HelpActivity).supportActionBar?.title = getString(R.string.about)
        _binding = null
    }

}
