# KMM-Nasa-Explorer
Repository to migrate the existing Nasa Explorer Android App in this repository https://github.com/MarioManzanoCulebras/Nasa-Explorer to be Multiplatform and start running it in destktop and IOs platforms using Kotlin Multiplatform.

Quick Guide to migrate Android Application to run on Android, desktop and IOs using Kotlin Multiplatform
https://medium.com/@manzanus/quick-guide-to-migrate-android-application-to-run-on-android-desktop-and-ios-using-kotlin-928a783c3491

Some notes for creating the distributable runner of the app on Desktop:
  - In desktopMain/kotlin/com/mariomanzano/kmm_nasa_explorer/Platform.desktop.kt

    single<SqlDriver> {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NasaDatabase.Schema.create(driver)
        driver
    }
    
  - And modify desktop/build.gradle.kts to use the correct Java home path on your computer:

    application {
        javaHome = "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" (...)
  
  - For creating a desktop distributable follow the instructions on this link to createDistributable using gradle: https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution

Important note: This project requires the JDK 11 installed on the machine. This can be downloaded from Oracle:
https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
