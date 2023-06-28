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
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.iyps.R
import com.iyps.databinding.ActivityDetailsBinding
import com.iyps.fragments.details.DetailsFragment

class DetailsActivity : AppCompatActivity() {

    private lateinit var activityBinding: ActivityDetailsBinding
    lateinit var passwordLine: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        passwordLine = intent.getStringExtra("PwdLine")!!

        /*########################################################################################*/

        // Disable screenshots and screen recordings
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    
        activityBinding.detailsToolbar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        supportActionBar?.title = getString(R.string.details)

        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_host_fragment, DetailsFragment())
            .commitNow()

    }

}