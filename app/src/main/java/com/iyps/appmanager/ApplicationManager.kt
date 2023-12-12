/*
 * Copyright (c) 2022-present StellarSand
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

package com.iyps.appmanager

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.android.material.elevation.SurfaceColors
import com.iyps.R
import com.iyps.inputstream.ResourceFromInputStream
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.THEME_PREF
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.StandardKeyboards
import com.nulabinc.zxcvbn.Zxcvbn
import com.nulabinc.zxcvbn.ZxcvbnBuilder
import com.nulabinc.zxcvbn.matchers.DictionaryLoader
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class ApplicationManager : Application() {
    
    val preferenceManager by lazy {
        PreferenceManager(this)
    }
    
    private val commonPasswordsResource by lazy {
        val top200PasswordsStream = resources.openRawResource(R.raw.top_200_2023_passwords)
        val otherCommonPasswordsStream = resources.openRawResource(R.raw.other_common_passwords)
        
        val combinedStream =
            ByteArrayOutputStream().apply {
                top200PasswordsStream.copyTo(this)
                otherCommonPasswordsStream.copyTo(this)
            }.toByteArray()
        
        ResourceFromInputStream(ByteArrayInputStream(combinedStream))
    }
    
    private val englishWordsResource by lazy {
        val englishWordsStream = resources.openRawResource(R.raw.english_words)
        val effUnrankedStream = resources.openRawResource(R.raw.eff_unranked)
        
        val combinedStream =
            ByteArrayOutputStream().apply {
                englishWordsStream.copyTo(this)
                effUnrankedStream.copyTo(this)
            }.toByteArray()
        
        ResourceFromInputStream(ByteArrayInputStream(combinedStream))
    }
    
    private val darkwebPasswordsResource by lazy {
        val darkwebPasswordsStream = resources.openRawResource(R.raw.darkweb)
        ResourceFromInputStream(darkwebPasswordsStream)
    }
    
    private val frenchPasswordsResource by lazy {
        val frenchPasswordsStream = resources.openRawResource(R.raw.richelieu_french)
        ResourceFromInputStream(frenchPasswordsStream)
    }
    
    private val azulPasswordsResource by lazy {
        val azulPasswordsStream = resources.openRawResource(R.raw.unkown_azul)
        ResourceFromInputStream(azulPasswordsStream)
    }
    
    val zxcvbn: Zxcvbn by lazy {
        ZxcvbnBuilder()
            .dictionaries(listOf(DictionaryLoader("passwords", commonPasswordsResource).load(),
                                 DictionaryLoader("english_wikipedia", englishWordsResource).load(),
                                 DictionaryLoader("darkweb", darkwebPasswordsResource).load(),
                                 DictionaryLoader("richelieu_french", frenchPasswordsResource).load(),
                                 DictionaryLoader("unkown_azul", azulPasswordsResource).load(),
                                 StandardDictionaries.FEMALE_NAMES_LOADER.load(),
                                 StandardDictionaries.MALE_NAMES_LOADER.load(),
                                 StandardDictionaries.SURNAMES_LOADER.load(),
                                 StandardDictionaries.US_TV_AND_FILM_LOADER.load()))
            .keyboards(StandardKeyboards.loadAllKeyboards())
            .build()
    }
    
    val secureRandom: SecureRandom by lazy {
        try {
            when {
                Build.VERSION.SDK_INT >= 26 -> SecureRandom.getInstanceStrong()
                else -> SecureRandom()
            }
        }
        catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("SecureRandom algorithm not available", e)
        }
    }
    
    val passphraseWordsMap by lazy {
        val wordMap = mutableMapOf<String, String>()
        val effPassphraseWordsStream = resources.openRawResource(R.raw.eff_passphrase_words)
        
        BufferedReader(InputStreamReader(effPassphraseWordsStream))
            .useLines { lines ->
                lines.forEach { line ->
                    val (id, word) = line.split("\t")
                    wordMap[id] = word
                }
            }
        
        wordMap
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Theme
        when(preferenceManager.getInt(THEME_PREF)) {
            
            0 -> {
                if (Build.VERSION.SDK_INT >= 29){
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }
            }
            R.id.follow_system -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            R.id.light -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            R.id.dark -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        
        // Set status bar and navigation bar colors
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.apply {
                    statusBarColor = SurfaceColors.SURFACE_0.getColor(activity)
                    navigationBarColor = SurfaceColors.getColorForElevation(activity, 8f)
                }
            }
            
            override fun onActivityStarted(activity: Activity) {}
            
            override fun onActivityResumed(activity: Activity) {}
            
            override fun onActivityPaused(activity: Activity) {}
            
            override fun onActivityStopped(activity: Activity) {}
            
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            
            override fun onActivityDestroyed(activity: Activity) {}
        })
        
    }
    
}