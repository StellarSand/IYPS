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

package com.iyps.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iyps.R
import com.iyps.databinding.ActivityMainBinding
import com.iyps.fragments.help.AboutFragment
import com.iyps.fragments.help.ScoreHelpFragment

class HelpActivity : AppCompatActivity() {

    private lateinit var activityBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        /*########################################################################################*/

        setSupportActionBar(activityBinding.toolbarMain)
        activityBinding.toolbarMain.setNavigationIcon(R.drawable.ic_back)
        activityBinding.toolbarMain.setNavigationOnClickListener { onBackPressed() }
        activityBinding.bottomNav.visibility = View.GONE

        //activityBinding.tabLayout.visibility = View.GONE
        activityBinding.selectButton.visibility = View.GONE

        intent.getStringExtra("fragment")?.let { displayFragment(it) }

    }

    // Setup fragments
    private fun displayFragment(fragmentName: String) {

        lateinit var fragment: Fragment

        when (fragmentName) {

            "Score Help" -> {
                supportActionBar?.title = getString(R.string.help)
                fragment = ScoreHelpFragment()
            }
            "About" -> {
                supportActionBar?.title = getString(R.string.about)
                fragment = AboutFragment()

            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_host_fragment, fragment)
            .commitNow()
    }

}