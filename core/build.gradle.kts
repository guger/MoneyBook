import Dependencies.AndroidX
import Dependencies.Kotlin
import Dependencies.Libs
import Dependencies.Material
import Dependencies.Test

/*
 * Copyright 2020 Daniel Guger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(Versions.SDK.TARGET)

    defaultConfig {
        minSdkVersion(Versions.SDK.MIN)
        targetSdkVersion(Versions.SDK.TARGET)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.D8.CoreLibraryDesugaring)

    implementation(project(":data"))

    testImplementation(Test.Robolectric)

    kotlin(Kotlin.StandardLibray, Versions.Kotlin.Common)
    implementation(Kotlin.Coroutines)

    implementation(Dependencies.Koin.Android)

    implementation(AndroidX.Core)
    implementation(AndroidX.AppCompat)
    implementation(AndroidX.Annotation)
    implementation(AndroidX.Fragment)
    implementation(AndroidX.Preference)
    implementation(AndroidX.ConstraintLayout)

    implementation(Material.MaterialDesign)

    navigation()

    work()

    implementation(Libs.AssentCore)
    implementation(Libs.AssentRationales)
}