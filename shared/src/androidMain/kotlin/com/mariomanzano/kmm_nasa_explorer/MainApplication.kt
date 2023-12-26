package com.mariomanzano.kmm_nasa_explorer

import android.app.Application
import com.mariomanzano.kmm_nasa_explorer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            androidLogger()
            modules(appModule())
        }
    }
}