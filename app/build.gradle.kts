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
    alias(libs.plugins.android.kotlin)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "com.iyps"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iyps"
        minSdk = 23
        targetSdk = 36
        versionCode = 156
        versionName = "1.5.6"
        extensions.getByType<BasePluginExtension>().archivesName.set("${rootProject.name}_v$versionName")
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        /*getByName("debug") {
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

dependencies {
    implementation(libs.bundles.androidxCoreComponents)
    implementation(libs.material3)
    implementation(libs.koin.android)
    implementation(libs.bundles.navigation)
    implementation(libs.zxcvbn4j)
    implementation(libs.androidFastScrollKt)
}