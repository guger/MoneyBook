/*
 * Copyright 2021 Daniel Guger
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
@file:Suppress("ConstantConditionIf")

import Dependencies.AndroidX
import Dependencies.Firebase
import Dependencies.Plugins
import Dependencies.Test
import Dependencies.Kotlin
import Dependencies.Libs
import Dependencies.Material

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = Versions.SDK.TARGET

    defaultConfig {
        applicationId = Versions.App.ID
        minSdk = Versions.SDK.MIN
        targetSdk = Versions.SDK.TARGET

        versionCode = Versions.App.versionCode()
        versionName = Versions.App.versionName()

        testInstrumentationRunner = Test.TestInstrumentationRunner
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev-${Versions.App.buildSignature()}"
        }
        getByName("release") {
            if (Versions.App.isBeta()) versionNameSuffix = "-beta-" + Versions.App.Beta

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    lint {
        isAbortOnError = false
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.D8.CoreLibraryDesugaring)

    implementation(project(":core"))
    implementation(project(":data"))

    testImplementation(Test.JUnit)
    testImplementation(Test.Truth)
    testImplementation(Test.Robolectric)
    androidTestImplementation(Test.AndroidX.Extensions)
    androidTestImplementation(Test.AndroidX.EspressoCore)

    kotlin(Kotlin.StandardLibray, Versions.Kotlin.Common)
    implementation(Kotlin.Coroutines)

    koin()

    implementation(platform(Firebase.BoM))
    implementation(Firebase.Analytics)
    implementation(Firebase.Crashlytics)

    implementation(AndroidX.Core)
    implementation(AndroidX.AppCompat)
    implementation(AndroidX.Annotation)
    implementation(AndroidX.Fragment)
    implementation(AndroidX.Preference)
    implementation(AndroidX.ConstraintLayout)
    implementation(AndroidX.ViewPager2)
    implementation(AndroidX.RecylerView)
    implementation(AndroidX.CardView)

    implementation(Material.MaterialDesign)

    room()

    lifecycle()

    navigation()

    work()

    implementation(Libs.ViewPagerDots)
    implementation(Libs.MaterialCab)
    implementation(Libs.MaterialDialogsCore)
    implementation(Libs.MaterialDialogsColor)

    implementation(Libs.CalcDialog)
    implementation(Libs.IconDialog)
    implementation(Libs.RecurPicker)

    implementation(fileTree("libs"))
}

apply(plugin = Plugins.Name.GoogleServices)