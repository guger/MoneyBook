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

import Dependencies.AndroidX
import Dependencies.Arch
import Dependencies.Kotlin
import Dependencies.Libs
import Dependencies.Test

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

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(
                    mutableMapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true"
                    )
                )
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false

            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    coreLibraryDesugaring(Dependencies.D8.CoreLibraryDesugaring)

    testImplementation(Test.JUnit)
    testImplementation(Test.Robolectric)
    testImplementation(Test.Truth)
    testImplementation(Test.AndroidX.Extensions)
    testImplementation(Test.AndroidX.ArchCoreTesting)
    testImplementation(Test.AndroidX.EspressoCore)
    testImplementation(Test.AndroidX.EspressoIdlingResource)
    testImplementation(Arch.RoomTesting)

    kotlin(Kotlin.StandardLibray, Versions.Kotlin.Common)
    implementation(Kotlin.Coroutines)

    implementation(AndroidX.Core)
    implementation(AndroidX.AppCompat)
    implementation(AndroidX.Annotation)
    implementation(AndroidX.Fragment)
    implementation(AndroidX.Preference)

    lifecycle()

    room()

    work()

    moshi()

    implementation(Libs.RecurPicker)
}