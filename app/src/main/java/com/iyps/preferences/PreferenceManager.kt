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
        const val MATERIAL_YOU = "material_you"
        const val BLOCK_SS = "block_ss"
        const val INCOG_KEYBOARD = "incog_keyboard"
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
    }
    
    private val sharedPreferences =
        context.getSharedPreferences("com.iyps_preferences", Context.MODE_PRIVATE)

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun setInt(key: String, integer: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, integer)
        editor.apply()
    }
    
    fun getFloatDefVal20(key: String): Float {
        return sharedPreferences.getFloat(key, 20f)
    }
    
    fun getFloatDefVal5(key: String): Float {
        return sharedPreferences.getFloat(key, 5f)
    }
    
    fun setFloat(key: String, float: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat(key, float)
        editor.apply()
    }
    
    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, true)
    }
    
    fun getBooleanDefValFalse(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
    
    fun setBoolean(key: String, boolean: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, boolean)
        editor.apply()
    }
    
    fun getString(key: String): String? {
        return sharedPreferences.getString(key, "-")
    }
    
    fun setString(key: String, string: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, string)
        editor.apply()
    }

}