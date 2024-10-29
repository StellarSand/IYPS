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

package com.iyps.utils

import android.content.Context
import com.iyps.R
import java.io.InputStreamReader
import java.util.Collections
import java.util.Enumeration
import java.util.Locale
import java.util.Properties
import java.util.ResourceBundle

class LocaleUtils {
    
    companion object {
    
        fun localizedFeedbackResourceBundle(context: Context): ResourceBundle {
        
            val locale = Locale.getDefault().language
        
            // If locale is in zxcvbn4j, use default resource bundle
            // else use custom messages.properties from res/raw
            return if (locale !in setOf("fa", "sv", "tr", "zh")) {
                ResourceBundle.getBundle("com/nulabinc/zxcvbn/messages")
            }
            else {
                val properties = Properties().apply {
                    when(locale) {
                        "fa" -> load(InputStreamReader(context.resources.openRawResource(R.raw.messages_fa), Charsets.UTF_8))
                        "sv" -> load(InputStreamReader(context.resources.openRawResource(R.raw.messages_sv), Charsets.UTF_8))
                        "tr" -> load(InputStreamReader(context.resources.openRawResource(R.raw.messages_tr), Charsets.UTF_8))
                        "zh" -> load(InputStreamReader(context.resources.openRawResource(R.raw.messages_zh), Charsets.UTF_8))
                    }
                }
            
                object : ResourceBundle() {
                    override fun handleGetObject(key: String?): Any? {
                        return properties.getProperty(key)
                    }
                
                    override fun getKeys(): Enumeration<String> {
                        return Collections.enumeration(properties.keys.map { it.toString() })
                    }
                }
            }
        }
        
    }
    
}
