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

package com.iyps.adapters

import com.iyps.models.FileItem
import androidx.recyclerview.widget.RecyclerView
import com.iyps.adapters.FileItemAdapter.ListViewHolder
import android.widget.TextView
import com.iyps.R
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View

class FileItemAdapter(private val aListViewItems: List<FileItem>,
                      private val clickListener: OnItemClickListener): RecyclerView.Adapter<ListViewHolder>() {
    
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
    
    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        
        val passwordLine: TextView = itemView.findViewById(R.id.password_line)
        
        init {
            // Handle click events of items
            itemView.setOnClickListener(this)
        }
        
        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                clickListener.onItemClick(position)
            }
        }
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view, parent, false)
        return ListViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val fileItem = aListViewItems[position]
        holder.passwordLine.text = fileItem.passwordLine
        
        // Horizontally scrolling text
        //hScrollText(holder.passwordLine);
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
}