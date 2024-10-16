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

package com.iyps.appmanager

import android.app.Application
import android.os.Build
import com.iyps.R
import com.iyps.inputstream.ResourceFromInputStream
import com.iyps.models.MultiPwdItem
import com.iyps.preferences.PreferenceManager
import com.iyps.preferences.PreferenceManager.Companion.THEME_PREF
import com.iyps.utils.UiUtils.Companion.setAppTheme
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.StandardKeyboards
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
        ResourceFromInputStream(
            ByteArrayInputStream(ByteArrayOutputStream().apply {
                resources.openRawResource(R.raw.top_200_2023_passwords).copyTo(this)
                resources.openRawResource(R.raw.other_common_passwords).copyTo(this)
            }.toByteArray())
        )
    }
    
    private val wordsResource by lazy {
        ResourceFromInputStream(
            ByteArrayInputStream(ByteArrayOutputStream().apply {
                resources.openRawResource(R.raw.english_words).copyTo(this)
                resources.openRawResource(R.raw.eff_unranked).copyTo(this)
                resources.openRawResource(R.raw.italian_words).copyTo(this)
            }.toByteArray())
        )
    }
    
    private val darkwebPasswordsResource by lazy {
        ResourceFromInputStream(resources.openRawResource(R.raw.darkweb))
    }
    
    private val frenchPasswordsResource by lazy {
        ResourceFromInputStream(resources.openRawResource(R.raw.richelieu_french))
    }
    
    private val azulPasswordsResource by lazy {
        ResourceFromInputStream(resources.openRawResource(R.raw.unkown_azul))
    }
    
    private val namesResource by lazy {
        ResourceFromInputStream(
            ByteArrayInputStream(ByteArrayOutputStream().apply {
                resources.openRawResource(R.raw.english_female_names).copyTo(this)
                resources.openRawResource(R.raw.english_male_names).copyTo(this)
                resources.openRawResource(R.raw.italian_names).copyTo(this)
            }.toByteArray())
        )
    }
    
    private val surnamesResource by lazy {
        ResourceFromInputStream(
            ByteArrayInputStream(ByteArrayOutputStream().apply {
                resources.openRawResource(R.raw.english_surnames).copyTo(this)
                resources.openRawResource(R.raw.italian_surnames).copyTo(this)
                resources.openRawResource(R.raw.spanish_surnames).copyTo(this)
            }.toByteArray())
        )
    }
    
    // "english_wikipedia" contains english_words, eff_unranked & italian_words.
    // "female_names" contains english_female_names, english_male_names & italian_names.
    // The default dictionary names like "english_wikipedia" & "female_names" are used
    // so that default warnings matching those dictionary names can be displayed.
    // The dictionary names are later changed while displaying, in ResultUtils.getMatchSequenceText()
    val zxcvbn by lazy {
        ZxcvbnBuilder()
            .dictionaries(listOf(DictionaryLoader("passwords", commonPasswordsResource).load(),
                                 DictionaryLoader("english_wikipedia", wordsResource).load(),
                                 DictionaryLoader("darkweb", darkwebPasswordsResource).load(),
                                 DictionaryLoader("richelieu_french", frenchPasswordsResource).load(),
                                 DictionaryLoader("unkown_azul", azulPasswordsResource).load(),
                                 DictionaryLoader("female_names", namesResource).load(),
                                 DictionaryLoader("surnames", surnamesResource).load(),
                                 StandardDictionaries.US_TV_AND_FILM_LOADER.load()))
            .keyboards(StandardKeyboards.loadAllKeyboards())
            .build()
    }
    
    val secureRandom by lazy {
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
        
        BufferedReader(InputStreamReader(resources.openRawResource(R.raw.eff_passphrase_words)))
            .useLines { lines ->
                lines.forEach { line ->
                    val (id, word) = line.split("\t")
                    wordMap[id] = word
                }
            }
        
        wordMap
    }
    
    val multiPasswordsList by lazy { mutableListOf<MultiPwdItem>() }
    var isAppOpen = false
    
    override fun onCreate() {
        super.onCreate()
        
        // Theme
        setAppTheme(preferenceManager.getInt(THEME_PREF))
        
    }
    
}