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

package com.iyps.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.iyps.R
import com.iyps.activities.MainActivity
import com.iyps.models.License
import com.iyps.utils.IntentUtils.Companion.openURL

class LicenseItemAdapter(private val aListViewItems: ArrayList<License>,
                         private val mainActivity: MainActivity) : RecyclerView.Adapter<LicenseItemAdapter.ListViewHolder>() {
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val licenseTitle: MaterialTextView = itemView.findViewById(R.id.licenseTitle)
        val licenseDesc: MaterialTextView = itemView.findViewById(R.id.licenseDesc)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_licenses_recycler_view, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        val license = aListViewItems[position]
        
        holder.licenseTitle.apply {
            text = license.title
            setOnClickListener{
                openURL(mainActivity, license.url)
            }
        }
        
        holder.licenseDesc.text = license.desc
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
}