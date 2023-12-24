package com.mariomanzano.kmm_nasa_explorer.data.di

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesService
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesServiceImpl
import com.mariomanzano.kmm_nasa_explorer.network.PODServerDataSource
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureDetailViewModel
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun appModule() = module {
    singleOf(::DailyPicturesServiceImpl) { bind<DailyPicturesService>() }
    singleOf(::PODServerDataSource) { bind<PODRemoteDataSource>() }
    singleOf(::DailyPicturesRepository) { bind<DailyPicturesRepository>() }
    singleOf(::DailyPictureViewModel) { bind<DailyPictureViewModel>() }
    singleOf(::DailyPictureDetailViewModel) { bind<DailyPictureDetailViewModel>() }
}