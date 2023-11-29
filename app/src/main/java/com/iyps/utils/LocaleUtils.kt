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
            return if (locale !in setOf("sv", "tr")) {
                ResourceBundle.getBundle("com/nulabinc/zxcvbn/messages")
            }
            else {
                val properties = Properties().apply {
                    when(locale) {
                        "sv" -> load(InputStreamReader(context.resources.openRawResource(R.raw.messages_sv), Charsets.UTF_8))
                        "tr" -> load(InputStreamReader(context.resources.openRawResource(R.raw.messages_tr), Charsets.UTF_8))
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