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
            return if (locale !in setOf("cs", "et", "fa", "pt-rBR", "sv", "tr", "zh")) {
                ResourceBundle.getBundle("com/nulabinc/zxcvbn/messages")
            }
            else {
                val properties =
                    when(locale) {
                        "cs" -> loadTranslations(context, R.raw.messages_cs)
                        "et" -> loadTranslations(context, R.raw.messages_et)
                        "fa" -> loadTranslations(context, R.raw.messages_fa)
                        "pt-rBR" -> loadTranslations(context, R.raw.messages_pt_rbr)
                        "sv" -> loadTranslations(context, R.raw.messages_sv)
                        "tr" -> loadTranslations(context, R.raw.messages_tr)
                        else -> loadTranslations(context, R.raw.messages_zh)
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
        
        private fun loadTranslations(context: Context, resId: Int): Properties {
            return Properties().apply {
                load(InputStreamReader(context.resources.openRawResource(resId), Charsets.UTF_8))
            }
        }
        
    }
    
}
