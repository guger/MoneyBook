import org.gradle.api.artifacts.dsl.DependencyHandler

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
            const val Extensions = "androidx.test.ext:junit:${Versions.Test.AndroidX.JUnit}"
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
        const val Android = "io.insert-koin:koin-android:${Versions.KOIN}"
        const val WorkManager = "io.insert-koin:koin-androidx-workmanager:${Versions.KOIN}"
    }

    object Firebase {
        const val BoM = "com.google.firebase:firebase-bom:${Versions.Firebase.BoM}"
        const val Analytics = "com.google.firebase:firebase-analytics-ktx"
        const val Crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    }

    object Compose {
        const val UI = "androidx.compose.ui:ui:${Versions.Compose}"

        const val UITooling = "androidx.compose.ui:ui-tooling:${Versions.Compose}"

        const val Foundation = "androidx.compose.foundation:foundation:${Versions.Compose}"

        const val Material = "androidx.compose.material:material:${Versions.Compose}"
        const val MaterialComposeThemeAdapter = "com.google.android.material:compose-theme-adapter:${Versions.Compose}"

        const val IconsCore = "androidx.compose.material:material-icons-core:${Versions.Compose}"
        const val IconsExtended = "androidx.compose.material:material-icons-extended:${Versions.Compose}"

        const val ConstraintLayout = "androidx.constraintlayout:constraintlayout-compose:${Versions.ComposeConstraintLayout}"

        const val LiveData = "androidx.compose.runtime:runtime-livedata:${Versions.Compose}"
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
        const val LifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.Arch.Lifecycle}"
        const val LifecycleLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.Arch.Lifecycle}"
        const val LifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.Arch.Lifecycle}"

        const val NavigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.Arch.Navigation}"
        const val NavigationUI = "androidx.navigation:navigation-ui-ktx:${Versions.Arch.Navigation}"

        const val RoomRuntime = "androidx.room:room-runtime:${Versions.Arch.Room}"
        const val RoomKtx = "androidx.room:room-ktx:${Versions.Arch.Room}"
        const val RoomCompiler = "androidx.room:room-compiler:${Versions.Arch.Room}"
        const val RoomTesting = "androidx.room:room-testing:${Versions.Arch.Room}"

        const val WorkManager = "androidx.work:work-runtime-ktx:${Versions.Arch.Work}"
    }

    object Libs {
        const val MaterialCab = "com.afollestad:material-cab:${Versions.Libraries.MaterialCab}"
        const val MaterialDialogsCore = "com.afollestad.material-dialogs:core:${Versions.Libraries.MaterialDialogs}"
        const val MaterialDialogsColor = "com.afollestad.material-dialogs:color:${Versions.Libraries.MaterialDialogs}"
        const val MaterialDialogsFiles = "com.afollestad.material-dialogs:input:${Versions.Libraries.MaterialDialogs}"

        const val ViewPagerDots = "com.tbuonomo:dotsindicator:${Versions.Libraries.ViewPagerDots}"

        const val Moshi = "com.squareup.moshi:moshi:${Versions.Libraries.Moshi}"
        const val MoshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.Libraries.Moshi}"

        const val CalcDialog = "com.maltaisn:calcdialog:${Versions.Libraries.CalcDialog}"
        const val IconDialog = "com.maltaisn:icondialog:${Versions.Libraries.IconDialog}"
        const val RecurPicker = "com.maltaisn:recurpicker:${Versions.Libraries.RecurPicker}"
    }
}

fun DependencyHandler.koin() {
    implementation(Dependencies.Koin.Android)
    implementation(Dependencies.Koin.WorkManager)
}

fun DependencyHandler.compose() {
    implementation(Dependencies.Compose.UI)

    implementation(Dependencies.Compose.UITooling)

    implementation(Dependencies.Compose.Foundation)

    implementation(Dependencies.Compose.Material)
    implementation(Dependencies.Compose.MaterialComposeThemeAdapter)

    implementation(Dependencies.Compose.IconsCore)
    implementation(Dependencies.Compose.IconsExtended)

    implementation(Dependencies.Compose.LiveData)
}

fun DependencyHandler.lifecycle() {
    implementation(Dependencies.Arch.LifecycleViewModel)
    implementation(Dependencies.Arch.LifecycleLiveData)
    kapt(Dependencies.Arch.LifecycleCommon)
}

fun DependencyHandler.navigation() {
    implementation(Dependencies.Arch.NavigationFragment)
    implementation(Dependencies.Arch.NavigationUI)
}

fun DependencyHandler.room() {
    implementation(Dependencies.Arch.RoomRuntime)
    implementation(Dependencies.Arch.RoomKtx)
    kapt(Dependencies.Arch.RoomCompiler)
}

fun DependencyHandler.work() {
    implementation(Dependencies.Arch.WorkManager)
}

fun DependencyHandler.moshi() {
    implementation(Dependencies.Libs.Moshi)
    implementation(Dependencies.Libs.MoshiKotlin)
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