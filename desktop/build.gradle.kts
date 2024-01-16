import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
}

group = "com.mariomanzano.kmm_nasa_explorer"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(projects.shared)
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.core)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        javaHome = "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
        nativeDistributions {
            outputBaseDir.set(project.buildDir.resolve("OutputDir"))
            includeAllModules = true
        }
        mainClass = "com.mariomanzano.kmm_nasa_explorer.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KMM-Nasa-Explorer"
            packageVersion = "1.0.0"
        }
    }
}