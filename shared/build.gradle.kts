@file:Suppress("UnstableApiUsage", "OPT_IN_USAGE", "PropertyName")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

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
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.ktor)
            implementation(libs.koin.core)
            implementation(libs.bundles.arrow)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        iosMain.dependencies {
            implementation(libs.io.ktor.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.mariomanzano.kmm_nasa_explorer"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
}
