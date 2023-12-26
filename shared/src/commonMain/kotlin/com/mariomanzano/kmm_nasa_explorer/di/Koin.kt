package com.mariomanzano.kmm_nasa_explorer.di

import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.httpClient
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesService
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesServiceImpl
import com.mariomanzano.kmm_nasa_explorer.network.PODServerDataSource
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureViewModel
import com.mariomanzano.kmm_nasa_explorer.usecases.FindPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.GetPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODListUseCase
import io.ktor.client.HttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(appModule())
}

// called by iOS etc
fun initKoin() = initKoin {}

fun appModule() = module {
    single<HttpClient> { httpClient() }
    singleOf(::DailyPicturesServiceImpl) { bind<DailyPicturesService>() }
    singleOf(::PODServerDataSource) { bind<PODRemoteDataSource>() }
    singleOf(::DailyPicturesRepository) { bind<DailyPicturesRepository>() }
    singleOf(::GetPODUseCase) { bind<GetPODUseCase>() }
    singleOf(::FindPODUseCase) { bind<FindPODUseCase>() }
    singleOf(::RequestPODListUseCase) { bind<RequestPODListUseCase>() }
    singleOf(::DailyPictureViewModel) { bind<DailyPictureViewModel>() }
}