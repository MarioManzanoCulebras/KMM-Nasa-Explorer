@file:Suppress("UnstableApiUsage", "OPT_IN_USAGE", "PropertyName")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose)
    kotlin("plugin.serialization") version "1.9.21"
    id("app.cash.sqldelight") version "2.0.1"
}

// CocoaPods requires the podspec to have a version.
group = "com.mariomanzano.kmm_nasa_explorer"
version = "1.0-SNAPSHOT"

kotlin {
    kotlin.applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop") {
        jvmToolchain(11)
    }

    sourceSets.nativeMain.dependencies {
        implementation(libs.sqldelight.native.driver)
    }

    listOf(
        macosX64(),
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries {
            executable()
        }
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.json)
                implementation(libs.ktor.client.negotiation)
                implementation(libs.ktor.client.encoding)
                implementation(libs.koin.core)
                implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.core)
                implementation(libs.bundles.arrow)
                implementation(libs.kotlinx.datetime)
                implementation(libs.media.kamel.image)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(libs.voyager)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.sqldelight.adapters)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
        }

        val commonComposeKmpMain by creating {
            dependsOn(commonMain)
        }

        val desktopMain by getting {
            dependsOn(commonComposeKmpMain)
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(compose.desktop.currentOs)
                implementation(libs.sqldelight.jvm.driver)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
            }
        }

        val androidMain by getting {
            dependsOn(commonComposeKmpMain)
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.koin.android)
                implementation(libs.sqldelight.android.driver)
            }
        }

        val iosMain by getting {
            dependsOn(commonComposeKmpMain)
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }
    }
}

android {
    namespace = "com.mariomanzano.kmm_nasa_explorer"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
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

sqldelight {
    databases {
        create("NasaDatabase") {
            packageName.set("com.mariomanzano.kmm_nasa_explorer.shared.cache")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.BIN
}
