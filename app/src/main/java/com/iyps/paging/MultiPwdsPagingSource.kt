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

package com.iyps.paging

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.iyps.models.LinePointer
import com.iyps.objects.MultiPwdsInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.nio.ByteBuffer

class MultiPwdsPagingSource(
    private val context: Context,
    private val source: MultiPwdsInput.Source,
    private val sortedIndicesList: List<Int>, // For manually added
    private val sortedPointersList: List<LinePointer> // For selected file
) : PagingSource<Int, String>() {
    
    override fun getRefreshKey(state: PagingState<Int, String>): Int? {
        return state.anchorPosition
    }
    
    // This function runs automatically as the user scrolls.
    // It reads only the lines needed for the UI.
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, String> {
        val position = params.key ?: 0
        val ensurePageSize = params.loadSize
        val totalItems =
            if (source is MultiPwdsInput.Source.ManualInput) sortedIndicesList.size
            else sortedPointersList.size
        val endPosition = minOf(position + ensurePageSize, totalItems)
        
        if (position >= totalItems) {
            return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val loadedLines = mutableListOf<String>()
                
                when (source) {
                    is MultiPwdsInput.Source.ManualInput -> {
                        val originalLines = source.lines
                        (position until endPosition).forEach {
                            val originalIndex = sortedIndicesList[it]
                            loadedLines.add(originalLines[originalIndex])
                        }
                    }
                    is MultiPwdsInput.Source.FileInput -> {
                        // - Open a read only system pipeline to the text file on the device
                        // - Grab the pointer coordinates for only the requested range of items (e.g., 20 items)
                        // - Using fileChannel.position(), instantly jump to the exact byte offset where that specific line starts
                        // - Create a byte container, sized exactly to that line's length
                        // - Read those bytes > convert them to text > send them to RecyclerView adapter
                        context.contentResolver.openFileDescriptor(source.fileUri, "r")?.use { parcelFileDescriptor ->
                            FileInputStream(parcelFileDescriptor.fileDescriptor).channel.use { fileChannel ->
                                (position until endPosition).forEach {
                                    val pointer = sortedPointersList[it]
                                    fileChannel.position(pointer.byteOffset)
                                    val bytes = ByteArray(pointer.length)
                                    fileChannel.read(ByteBuffer.wrap(bytes))
                                    loadedLines.add(String(bytes, Charsets.UTF_8).trim())
                                }
                            }
                        }
                    }
                }
                
                LoadResult.Page(
                    data = loadedLines,
                    prevKey = if (position == 0) null else position - ensurePageSize,
                    nextKey = if (endPosition == totalItems) null else endPosition
                )
            }
            catch (e: Exception) {
                LoadResult.Error(e)
            }
        }
    }
}