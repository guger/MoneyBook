import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

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

object Versions {

    object App {
        const val ID = "at.guger.moneybook"
        const val Major = 2
        const val Minor = 3
        const val Patch = 0
        const val Release = 1
        const val Beta = 0
        const val Dev = 0

        fun versionCode(): Int = 21104000
        // fun versionCode(): Int = major() + minor() + patch() + release() + beta() + dev() TODO
        fun versionName() = "$Major.$Minor.$Patch"

        fun isBeta(): Boolean = Beta > 0

        private fun major() = Major * (10.0.pow(7)).toInt()
        private fun minor() = Minor * (10.0.pow(6)).toInt()
        private fun patch() = Patch * (10.0.pow(4)).toInt()
        private fun release() = Release * (10.0.pow(2)).toInt()
        private fun beta() = Beta * 10
        private fun dev() = Dev

        fun buildSignature(): String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    object SDK {
        const val MIN = 21
        const val TARGET = 31
    }

    const val Gradle = "7.2.0"

    const val KOIN = "3.1.3"

    object D8 {
        const val CoreLibraryDesugaring = "1.1.5"
    }

    object Test {
        const val JUnit = "4.13.1"
        const val Robolectric = "4.7.2"
        const val Truth = "1.1.3"

        object AndroidX {
            const val Core = "1.4.0"
            const val Espresso = "3.4.0"
            const val JUnit = "1.1.3"
            const val ArchCoreTesting = "2.1.0"
        }
    }

    object Kotlin {
        const val Common = "1.6.21"
        const val Coroutines = "1.6.1"
    }

    object Firebase {

        object Plugins {
            const val GoogleServices = "4.3.10"
            const val CrashlyticsGradle = "2.8.1"
        }

        const val BoM = "30.0.0"
    }

    const val Compose = "1.2.0-beta02"
    const val ComposeConstraintLayout = "1.0.0"

    object AndroidX {
        const val Core = "1.8.0-rc02"
        const val CoreSplashScreen = "1.0.0-rc01"
        const val AppCompat = "1.6.0-alpha03"
        const val Annotation = "1.4.0-beta01"
        const val Fragment = "1.5.0-rc01"
        const val Preference = "1.2.0"
        const val Biometric = "1.2.0-alpha04"
        const val ViewPager2 = "1.1.0-beta01"
        const val ConstraintLayout = "2.1.3"
        const val RecyclerView = "1.2.1"
        const val CardView = "1.0.0"
    }

    object Material {
        const val Material = "1.6.0"
    }

    object Arch {
        const val Lifecycle = "2.5.0-rc01"
        const val Navigation = "2.5.0-rc01"
        const val Room = "2.4.2"
        const val Work = "2.7.1"
    }

    object Libraries {
        const val MaterialCab = "2.0.1"
        const val MaterialDialogs = "3.3.0"

        const val ViewPagerDots = "4.2"

        const val Moshi = "1.13.0"

        const val CalcDialog = "2.2.1"
        const val IconDialog = "3.3.0"
        const val RecurPicker = "2.1.6"
    }
}
