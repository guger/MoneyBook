import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

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

object Versions {

    object App {
        const val ID = "at.guger.moneybook"
        const val Major = 2
        const val Minor = 0
        const val Patch = 0
        const val Release = 0
        const val Beta = 13
        const val Dev = 0

        fun versionCode(): Int = major() + minor() + patch() + release() + beta() + dev()
        fun versionName() = "$Major.$Minor.$Patch"

        fun isBeta(): Boolean = Beta > 0

        private fun major() = Major * (10.0.pow(7)).toInt()
        private fun minor() = Minor * (10.0.pow(6)).toInt()
        private fun patch() = Patch * (10.0.pow(4)).toInt()
        private fun release() = Release * (10.0.pow(3)).toInt()
        private fun beta() = Beta * 10
        private fun dev() = Dev

        fun buildSignature(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    object SDK {
        const val MIN = 21
        const val TARGET = 30
    }

    const val Gradle = "4.2.0-beta04"

    const val KOIN = "2.2.2"

    object D8 {
        const val CoreLibraryDesugaring = "1.0.9"
    }

    object Test {
        const val JUnit = "4.13.1"
        const val Robolectric = "4.4"
        const val Truth = "1.0"

        object AndroidX {
            const val Extensions = "1.1.1"
            const val ArchCoreTesting = "2.1.0"
            const val Espresso = "3.3.0"
        }
    }

    object Kotlin {
        const val Common = "1.4.21"
        const val Coroutines = "1.4.1"
    }

    object Firebase {

        object Plugins {
            const val GoogleServices = "4.3.5"
            const val CrashlyticsGradle = "2.4.1"
        }

        const val Analytics = "18.0.2"
        const val Crashlytics = "17.3.1"
    }

    object AndroidX {
        const val Core = "1.5.0-beta01"
        const val AppCompat = "1.2.0"
        const val Annotation = "1.2.0-beta01"
        const val Fragment = "1.3.0"
        const val Preference = "1.1.1"
        const val Biometric = "1.2.0-alpha02"
        const val ViewPager2 = "1.0.0"
        const val ConstraintLayout = "2.0.4"
        const val RecyclerView = "1.2.0-beta01"
        const val CardView = "1.0.0"
    }

    object Material {
        const val Material = "1.3.0"
    }

    object Arch {
        const val Lifecycle = "2.3.0"
        const val Navigation = "2.3.3"
        const val Room = "2.3.0-beta01"
        const val Work = "2.5.0"
    }

    object Libraries {
        const val Assent = "3.0.0-RC4"
        const val MaterialCab = "2.0.1"
        const val MaterialDialogs = "3.3.0"

        const val ViewPagerDots = "4.1.2"

        const val CalcDialog = "2.2.1"
        const val IconDialog = "3.3.0"
        const val RecurPicker = "2.1.3"
    }
}
