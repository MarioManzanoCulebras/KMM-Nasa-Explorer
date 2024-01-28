# KMM-Nasa-Explorer
Repository to migrate the existing Nasa Explorer Android App in this repository https://github.com/MarioManzanoCulebras/Nasa-Explorer to be Multiplatform and start running it in destktop and IOs platforms using Kotlin Multiplatform.

TODO: Include Medium articule

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
