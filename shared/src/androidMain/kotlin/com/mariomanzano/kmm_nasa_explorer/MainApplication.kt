package com.mariomanzano.kmm_nasa_explorer

import android.app.Application
import android.content.Context
import initKoin
import org.koin.dsl.module

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(
            listOf(
                module {
                    single<Context> { this@MainApplication }
                }
            )
        )
    }
}