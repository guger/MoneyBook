import org.gradle.api.artifacts.dsl.DependencyHandler

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

object Dependencies {

    object Plugins {
        object Path {
            const val Gradle = "com.android.tools.build:gradle:${Versions.Gradle}"
            const val NavigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.Arch.Navigation}"
            const val GoogleServices = "com.google.gms:google-services:${Versions.Firebase.Plugins.GoogleServices}"
            const val CrashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:${Versions.Firebase.Plugins.CrashlyticsGradle}"
        }

        object Name {
            const val KotlinGradle = "gradle-plugin"
            const val GoogleServices = "com.google.gms.google-services"
        }
    }

    object D8 {
        const val CoreLibraryDesugaring = "com.android.tools:desugar_jdk_libs:${Versions.D8.CoreLibraryDesugaring}"
    }

    object Test {
        const val TestInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        const val JUnit = "junit:junit:${Versions.Test.JUnit}"
        const val Robolectric = "org.robolectric:robolectric:${Versions.Test.Robolectric}"
        const val Truth = "com.google.truth:truth:${Versions.Test.Truth}"

        object AndroidX {
            const val Extensions = "androidx.test.ext:junit:${Versions.Test.AndroidX.Extensions}"
            const val ArchCoreTesting = "androidx.arch.core:core-testing:${Versions.Test.AndroidX.ArchCoreTesting}"
            const val EspressoCore = "androidx.test.espresso:espresso-core:${Versions.Test.AndroidX.Espresso}"
            const val EspressoIdlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.Test.AndroidX.Espresso}"
        }
    }

    object Kotlin {
        const val StandardLibray = "stdlib-jdk8"
        const val Coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.Coroutines}"
    }

    object Koin {
        const val Android = "org.koin:koin-android:${Versions.Koin}"
        const val Scope = "org.koin:koin-androidx-scope:${Versions.Koin}"
        const val ViewModel = "org.koin:koin-androidx-viewmodel:${Versions.Koin}"
    }

    object Firebase {
        const val Analytics = "com.google.firebase:firebase-analytics:${Versions.Firebase.Analytics}"
        const val Crashlytics = "com.google.firebase:firebase-crashlytics:${Versions.Firebase.Crashlytics}"
    }

    object AndroidX {
        const val Core = "androidx.core:core-ktx:${Versions.AndroidX.Core}"
        const val AppCompat = "androidx.appcompat:appcompat:${Versions.AndroidX.AppCompat}"
        const val Annotation = "androidx.annotation:annotation:${Versions.AndroidX.Annotation}"
        const val Fragment = "androidx.fragment:fragment-ktx:${Versions.AndroidX.Fragment}"
        const val Preference = "androidx.preference:preference-ktx:${Versions.AndroidX.Preference}"
        const val ConstraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.ConstraintLayout}"
        const val ViewPager2 = "androidx.viewpager2:viewpager2:${Versions.AndroidX.ViewPager2}"
        const val RecylerView = "androidx.recyclerview:recyclerview:${Versions.AndroidX.RecyclerView}"
        const val CardView = "androidx.cardview:cardview:${Versions.AndroidX.CardView}"
    }

    object Material {
        const val MaterialDesign = "com.google.android.material:material:${Versions.Material.Material}"
    }

    object Arch {
        const val LifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.Arch.Lifecycle}"
        const val LifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.Arch.Lifecycle}"
        const val LifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.Arch.Lifecycle}"

        const val NavigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.Arch.Navigation}"
        const val NavigationUI = "androidx.navigation:navigation-ui-ktx:${Versions.Arch.Navigation}"

        const val RoomRuntime = "androidx.room:room-runtime:${Versions.Arch.Room}"
        const val RoomKTX = "androidx.room:room-ktx:${Versions.Arch.Room}"
        const val RoomCompiler = "androidx.room:room-compiler:${Versions.Arch.Room}"
        const val RoomTesting = "androidx.room:room-testing:${Versions.Arch.Room}"

        const val WorkManager = "androidx.work:work-runtime-ktx:${Versions.Arch.Work}"
    }

    object Libs {
        const val AssentCore = "com.afollestad.assent:core:${Versions.Libraries.Assent}"
        const val AssentRationales = "com.afollestad.assent:rationales:${Versions.Libraries.Assent}"
        const val MaterialCab = "com.afollestad:material-cab:${Versions.Libraries.MaterialCab}"
        const val MaterialDialogsCore = "com.afollestad.material-dialogs:core:${Versions.Libraries.MaterialDialogs}"
        const val MaterialDialogsColor = "com.afollestad.material-dialogs:color:${Versions.Libraries.MaterialDialogs}"

        const val Charts = "com.github.PhilJay:MPAndroidChart:${Versions.Libraries.Charts}"

        const val CalcDialog = "com.maltaisn:calcdialog:${Versions.Libraries.CalcDialog}"
        const val IconDialog = "com.maltaisn:icondialog:${Versions.Libraries.IconDialog}"
        const val RecurPicker = "com.maltaisn:recurpicker:${Versions.Libraries.RecurPicker}"
    }
}

fun DependencyHandler.koin() {
    implementation(Dependencies.Koin.Android)
    implementation(Dependencies.Koin.Scope)
    implementation(Dependencies.Koin.ViewModel)
}

fun DependencyHandler.lifecycle() {
    implementation(Dependencies.Arch.LifecycleExtensions)
    implementation(Dependencies.Arch.LifecycleViewModel)
    kapt(Dependencies.Arch.LifecycleCommon)
}

fun DependencyHandler.navigation() {
    implementation(Dependencies.Arch.NavigationFragment)
    implementation(Dependencies.Arch.NavigationUI)
}

fun DependencyHandler.room() {
    implementation(Dependencies.Arch.RoomRuntime)
    implementation(Dependencies.Arch.RoomKTX)
    kapt(Dependencies.Arch.RoomCompiler)
}

fun DependencyHandler.work() {
    implementation(Dependencies.Arch.WorkManager)
}

fun DependencyHandler.implementation(dependency: String) {
    add("implementation", dependency)
}

private fun DependencyHandler.kapt(dependency: String) {
    add("kapt", dependency)
}

private fun DependencyHandler.api(dependency: String) {
    add("api", dependency)
}