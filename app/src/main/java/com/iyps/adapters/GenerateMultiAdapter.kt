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

import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.iyps.R
import com.iyps.activities.DetailsActivity
import com.iyps.activities.MainActivity
import com.iyps.models.MultiPwdItem
import com.iyps.utils.ClipboardUtils.Companion.hideSensitiveContent
import com.iyps.utils.UiUtils.Companion.setButtonTooltipText
import com.iyps.utils.UiUtils.Companion.setGenPwdTextWithColor
import com.iyps.utils.UiUtils.Companion.showSnackbar

class GenerateMultiAdapter(private val aListViewItems: ArrayList<MultiPwdItem>,
                           private val mainActivity: MainActivity) : RecyclerView.Adapter<GenerateMultiAdapter.ListViewHolder>() {
    
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        val pwdLine: MaterialTextView = itemView.findViewById(R.id.genMultiItemLine)
        val detailsBtn: MaterialButton = itemView.findViewById(R.id.genMultiItemDetailsBtn)
        val copyBtn: MaterialButton = itemView.findViewById(R.id.genMultiItemCopyBtn)
        val shareBtn: MaterialButton = itemView.findViewById(R.id.genMultiItemShareBtn)
        
    }
    
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_generate_multiple, parent, false)
        )
    }
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        
        // Generated password
        holder.pwdLine.setGenPwdTextWithColor(aListViewItems[position].passwordLine)
        
        // Details
        holder.detailsBtn.apply {
            setButtonTooltipText(context.getString(R.string.details))
            setOnClickListener {
                context.startActivity(Intent(context, DetailsActivity::class.java)
                                  .putExtra("PwdLine", holder.pwdLine.text.toString()),
                              ActivityOptions.makeSceneTransitionAnimation(mainActivity).toBundle())
            }
        }
        
        // Copy
        holder.copyBtn.apply {
            setButtonTooltipText(context.getString(R.string.copy))
            setOnClickListener {
                val clipData = ClipData.newPlainText("", holder.pwdLine.text.toString())
                clipData.hideSensitiveContent()
                (context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clipData)
                // Only show snackbar in 12L or lower to avoid duplicate notifications
                // https://developer.android.com/develop/ui/views/touch-and-input/copy-paste#duplicate-notifications
                if (Build.VERSION.SDK_INT <= 32) {
                    showSnackbar(mainActivity.activityBinding.mainCoordLayout,
                                 context.getString(R.string.copied_to_clipboard),
                                 mainActivity.activityBinding.mainBottomNav)
                }
            }
        }
        
        // Share
        holder.shareBtn.apply {
            setButtonTooltipText(context.getString(R.string.share))
            setOnClickListener {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND)
                                                       .setType("text/plain")
                                                       .putExtra(Intent.EXTRA_TEXT, holder.pwdLine.text.toString()),
                                                   context.getString(R.string.share)))
            }
        }
    }
    
    override fun getItemCount(): Int {
        return aListViewItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
}