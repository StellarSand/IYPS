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

package com.iyps.preferences

import android.content.Context

class PreferenceManager(context: Context) {
    
    companion object {
        // Shared pref keys
        const val THEME_PREF = "theme"
        const val GRID_VIEW = "grid_view"
        const val SORT_ASC = "sort_asc"
        const val GEN_RADIO = "gen_radio"
        const val PWD_LENGTH = "pwd_length"
        const val PWD_UPPERCASE = "pwd_uppercase"
        const val PWD_LOWERCASE = "pwd_lowercase"
        const val PWD_NUMBERS = "pwd_numbers"
        const val PWD_SPEC_CHARS = "pwd_spec_chars"
        const val PWD_AMB_CHARS = "pwd_amb_chars"
        const val PWD_SPACES = "pwd_spaces"
        const val PHRASE_WORDS = "phrase_words"
        const val PHRASE_SEPARATOR = "phrase_separator"
        const val PHRASE_CAPITALIZE = "phrase_capitalize"
        const val MATERIAL_YOU = "material_you"
        const val BLOCK_SS = "block_ss"
        const val INCOG_KEYBOARD = "incog_keyboard"
    }
    
    private val sharedPreferences =
        context.getSharedPreferences("com.iyps_preferences", Context.MODE_PRIVATE)
    
    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }
    
    fun setInt(key: String, value: Int) {
        sharedPreferences.edit().apply {
            putInt(key, value)
            apply()
        }
    }
    
    fun getFloat(key: String, defValue: Float = 20f): Float {
        return sharedPreferences.getFloat(key, defValue)
    }
    
    fun setFloat(key: String, value: Float) {
        sharedPreferences.edit().apply {
            putFloat(key, value)
            apply()
        }
    }
    
    fun getBoolean(key: String, defValue: Boolean = true): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }
    
    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(key, value)
            apply()
        }
    }
    
    fun getString(key: String): String? {
        return sharedPreferences.getString(key, "-")
    }
    
    fun setString(key: String, value: String) {
        sharedPreferences.edit().apply {
            putString(key, value)
            apply()
        }
    }
    
}