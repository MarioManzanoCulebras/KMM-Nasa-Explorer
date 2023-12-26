package com.mariomanzano.kmm_nasa_explorer

import com.mariomanzano.kmm_nasa_explorer.di.appModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModule())
    }.koin
}