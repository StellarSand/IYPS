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

plugins {
    alias(libs.plugins.android.application)
    id("kotlin-parcelize")
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.iyps"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.iyps"
        minSdk = 26
        targetSdk = 37
        versionCode = 160
        versionName = "1.6.0"
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        /*debug {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }*/
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

base {
    archivesName.set("${rootProject.name}_v${android.defaultConfig.versionName}")
}

dependencies {
    implementation(libs.bundles.androidxCoreComponents)
    implementation(libs.material3)
    implementation(libs.koin.android)
    implementation(libs.bundles.navigation)
    implementation(libs.zxcvbn4j)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidFastScrollKt)
    implementation(libs.lottie)
}