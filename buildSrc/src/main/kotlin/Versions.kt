import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

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

object Versions {

    object App {
        const val ID = "at.guger.moneybook"
        const val Major = 2
        const val Minor = 0
        const val Patch = 0
        const val Release = 0
        const val Beta = 0
        const val Dev = 1

        fun versionCode(): Int = major() + minor() + patch() + release() + beta() + dev()
        fun versionName() = "$Major.$Minor.$Patch"

        fun isBeta(): Boolean = Beta > 0

        private fun major() = Major * (10.0.pow(5)).toInt()
        private fun minor() = Minor * (10.0.pow(4)).toInt()
        private fun patch() = Patch * (10.0.pow(3)).toInt()
        private fun release() = Release * (10.0.pow(2)).toInt()
        private fun beta() = Beta * 10
        private fun dev() = Dev

        fun buildSignature(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    }

    object SDK {
        const val MIN = 21
        const val TARGET = 30
    }

    const val Gradle = "4.1.0-beta05"

    const val Koin = "2.1.6"

    object D8 {
        const val CoreLibraryDesugaring = "1.0.9"
    }

    object Test {
        const val JUnit = "4.12"
        const val Robolectric = "4.3.1"
        const val Truth = "1.0"

        object AndroidX {
            const val Extensions = "1.1.1"
            const val ArchCoreTesting = "2.1.0"
            const val Espresso = "3.2.0"
        }
    }

    object Kotlin {
        const val Common = "1.3.72"
        const val Coroutines = "1.3.8"
    }

    object Firebase {

        object Plugins {
            const val GoogleServices = "4.3.3"
            const val CrashlyticsGradle = "2.2.0"
        }

        const val Analytics = "17.4.4"
        const val Crashlytics = "17.1.1"
    }

    object AndroidX {
        const val Core = "1.3.1"
        const val AppCompat = "1.2.0-rc02"
        const val Annotation = "1.1.0"
        const val Fragment = "1.2.5"
        const val Preference = "1.1.1"
        const val Biometric = "1.0.1"
        const val ViewPager2 = "1.0.0"
        const val ConstraintLayout = "2.0.0-rc1"
        const val RecyclerView = "1.1.0"
        const val CardView = "1.0.0"
    }

    object Material {
        const val Material = "1.3.0-alpha02"
        const val Flexbox = "1.1.0"
    }

    object Arch {
        const val Lifecycle = "2.2.0"
        const val Navigation = "2.3.0"
        const val Room = "2.2.5"
        const val Work = "2.4.0"
    }

    object Libraries {
        const val Assent = "3.0.0-RC4"
        const val MaterialCab = "2.0.1"
        const val MaterialDialogs = "3.3.0"

        const val Charts = "v3.1.0"
        const val ViewPagerDots = "4.1.2"

        const val CalcDialog = "2.2.1"
        const val IconDialog = "3.3.0"
        const val RecurPicker = "2.1.1"
    }
}
