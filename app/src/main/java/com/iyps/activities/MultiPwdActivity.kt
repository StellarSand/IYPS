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

package com.iyps.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iyps.adapters.MultiPwdAdapter
import com.iyps.appmanager.ApplicationManager
import com.iyps.databinding.ActivityMultiPwdBinding
import com.iyps.models.MultiPwdItem
import com.iyps.preferences.PreferenceManager
import com.iyps.utils.UiUtils.Companion.blockScreenshots
import me.stellarsand.android.fastscroll.FastScrollerBuilder

class MultiPwdActivity : AppCompatActivity(), MultiPwdAdapter.OnItemClickListener {
    
    private lateinit var activityBinding: ActivityMultiPwdBinding
    private lateinit var multiplePwdList: List<MultiPwdItem>
    private lateinit var appManager: ApplicationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityMultiPwdBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)
        
        appManager = applicationContext as ApplicationManager
        multiplePwdList = appManager.multiPasswordsList.sortedBy { it.passwordLine }
        val preferenceManager = appManager.preferenceManager
        
        // Disable screenshots and screen recordings
        blockScreenshots(this, preferenceManager.getBoolean(PreferenceManager.BLOCK_SS))
        
        activityBinding.multiPwdToolbar.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        activityBinding.recyclerView.apply {
            adapter = MultiPwdAdapter(multiplePwdList, this@MultiPwdActivity)
            FastScrollerBuilder(this).build()
        }
    }
    
    // On click
    override fun onItemClick(position: Int) {
        startActivity(Intent(this, DetailsActivity::class.java)
                          .putExtra("PwdLine", multiplePwdList[position].passwordLine))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        appManager.multiPasswordsList.clear()
    }
}