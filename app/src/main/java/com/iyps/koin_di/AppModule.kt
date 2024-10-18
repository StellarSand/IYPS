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

package com.iyps.koin_di

import android.app.Application
import android.os.Build
import com.iyps.R
import com.iyps.inputstream.ResourceFromInputStream
import com.iyps.preferences.PreferenceManager
import com.nulabinc.zxcvbn.StandardDictionaries
import com.nulabinc.zxcvbn.StandardKeyboards
import com.nulabinc.zxcvbn.ZxcvbnBuilder
import com.nulabinc.zxcvbn.matchers.DictionaryLoader
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import kotlin.sequences.forEach

val appModule =
    module {
        single { PreferenceManager(get()) }
        single(named("commonPasswordsResource")) {
            ResourceFromInputStream(
                ByteArrayInputStream(ByteArrayOutputStream().apply {
                    get<Application>().resources.openRawResource (R.raw.top_200_2023_passwords).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.other_common_passwords).copyTo(this)
                }.toByteArray())
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single(named("wordsResource")) {
            ResourceFromInputStream(
                ByteArrayInputStream(ByteArrayOutputStream().apply {
                    get<Application>().resources.openRawResource(R.raw.english_words).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.eff_unranked).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.italian_words).copyTo(this)
                }.toByteArray())
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single(named("darkwebPasswordsResource")) {
            ResourceFromInputStream(
                get<Application>().resources.openRawResource(R.raw.darkweb)
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single(named("frenchPasswordsResource")) {
            ResourceFromInputStream(
                get<Application>().resources.openRawResource(R.raw.richelieu_french)
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single(named("azulPasswordsResource")) {
            ResourceFromInputStream(
                get<Application>().resources.openRawResource(R.raw.unkown_azul)
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single(named("namesResource")) {
            ResourceFromInputStream(
                ByteArrayInputStream(ByteArrayOutputStream().apply {
                    get<Application>().resources.openRawResource(R.raw.english_female_names).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.english_male_names).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.italian_names).copyTo(this)
                }.toByteArray())
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single(named("surnamesResource")) {
            ResourceFromInputStream(
                ByteArrayInputStream(ByteArrayOutputStream().apply {
                    get<Application>().resources.openRawResource(R.raw.english_surnames).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.italian_surnames).copyTo(this)
                    get<Application>().resources.openRawResource(R.raw.spanish_surnames).copyTo(this)
                }.toByteArray())
            ) as com.nulabinc.zxcvbn.io.Resource
        }
        single {
            // "english_wikipedia" contains english_words, eff_unranked & italian_words.
            // "female_names" contains english_female_names, english_male_names & italian_names.
            // The default dictionary names like "english_wikipedia" & "female_names" are used
            // so that default warnings matching those dictionary names can be displayed.
            // The dictionary names are later changed while displaying, in ResultUtils.getMatchSequenceText()
            ZxcvbnBuilder()
                .dictionaries(listOf(DictionaryLoader("passwords", get(named("commonPasswordsResource"))).load(),
                                     DictionaryLoader("english_wikipedia", get(named("wordsResource"))).load(),
                                     DictionaryLoader("darkweb", get(named("darkwebPasswordsResource"))).load(),
                                     DictionaryLoader("richelieu_french", get(named("frenchPasswordsResource"))).load(),
                                     DictionaryLoader("unkown_azul", get(named("azulPasswordsResource"))).load(),
                                     DictionaryLoader("female_names", get(named("namesResource"))).load(),
                                     DictionaryLoader("surnames", get(named("surnamesResource"))).load(),
                                     StandardDictionaries.US_TV_AND_FILM_LOADER.load()))
                .keyboards(StandardKeyboards.loadAllKeyboards())
                .build()
        }
        single {
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
        single {
            val wordMap = mutableMapOf<String, String>()
            BufferedReader(InputStreamReader(get<Application>().resources.openRawResource(R.raw.eff_passphrase_words)))
                .useLines { lines ->
                    lines.forEach { line ->
                        val (id, word) = line.split("\t")
                        wordMap[id] = word
                    }
                }
            wordMap
        }
    }