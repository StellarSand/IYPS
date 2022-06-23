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

package com.iyps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyps.R;
import com.iyps.models.FileItem;

import java.util.List;

public class FileItemAdapter extends RecyclerView.Adapter<FileItemAdapter.ListViewHolder> {

    private final List<FileItem> aListViewItems;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        itemClickListener = clickListener;
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView passwordLine;

        public ListViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            passwordLine = itemView.findViewById(R.id.password_line);

            // Handle click events of items
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });

        }
    }

    public FileItemAdapter(List<FileItem> listViewItems)
    {
        aListViewItems = listViewItems;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public FileItemAdapter.ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        return new FileItemAdapter.ListViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final FileItemAdapter.ListViewHolder holder, int position) {

        final FileItem fileItem = aListViewItems.get(position);

        holder.passwordLine.setText(fileItem.getPasswordLine());

        // Horizontally scrolling text
        //hScrollText(holder.passwordLine);

    }

    @Override
    public int getItemCount() {
        return aListViewItems.size();
    }

    @Override
    public  int getItemViewType(int position){
        return position;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

}
