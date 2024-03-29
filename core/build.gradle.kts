import Dependencies.AndroidX
import Dependencies.Kotlin
import Dependencies.Material
import Dependencies.Test

/*
 * Copyright 2022 Daniel Guger
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
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    compileSdk = Versions.SDK.TARGET

    defaultConfig {
        minSdk = Versions.SDK.MIN
        targetSdk = Versions.SDK.TARGET

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
        viewBinding = true
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.D8.CoreLibraryDesugaring)

    implementation(project(":data"))

    testImplementation(Test.JUnit)
    testImplementation(Test.Truth)
    testImplementation(Test.Robolectric)

    kotlin(Kotlin.StandardLibray, Versions.Kotlin.Common)
    implementation(Kotlin.Coroutines)

    implementation(Dependencies.Koin.Android)

    implementation(AndroidX.Core)
    implementation(AndroidX.AppCompat)
    implementation(AndroidX.Annotation)
    implementation(AndroidX.Biometric)
    implementation(AndroidX.Fragment)
    implementation(AndroidX.Preference)
    implementation(AndroidX.ConstraintLayout)

    implementation(Material.MaterialDesign)

    navigation()

    work()
}