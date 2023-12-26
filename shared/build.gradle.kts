@file:Suppress("UnstableApiUsage", "OPT_IN_USAGE", "PropertyName")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
}

// CocoaPods requires the podspec to have a version.
version = "1.0"

kotlin {
    kotlin.applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.koin.core)
                implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
                implementation(libs.bundles.arrow)
                implementation(libs.kotlinx.datetime)
                implementation(libs.media.kamel.image)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(libs.voyager)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        val commonComposeKmpMain by creating {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(commonComposeKmpMain)
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.client.gson)
                implementation(libs.ktor.client.negotiation)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.koin.android)
            }
        }

        val iosMain by getting {
            dependsOn(commonComposeKmpMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

android {
    namespace = "com.mariomanzano.kmm_nasa_explorer"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
