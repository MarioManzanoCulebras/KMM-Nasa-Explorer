package com.mariomanzano.kmm_nasa_explorer.di

import com.mariomanzano.kmm_nasa_explorer.data.database.PODRoomDataSource
import com.mariomanzano.kmm_nasa_explorer.data.repositories.DailyPicturesRepository
import com.mariomanzano.kmm_nasa_explorer.datasources.PODLocalDataSource
import com.mariomanzano.kmm_nasa_explorer.datasources.PODRemoteDataSource
import com.mariomanzano.kmm_nasa_explorer.httpClient
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesService
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesServiceImpl
import com.mariomanzano.kmm_nasa_explorer.network.PODServerDataSource
import com.mariomanzano.kmm_nasa_explorer.platformModule
import com.mariomanzano.kmm_nasa_explorer.shared.cache.Database
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.DailyPictureViewModel
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.FavoriteDetailViewModel
import com.mariomanzano.kmm_nasa_explorer.ui.viewmodels.FavoriteViewModel
import com.mariomanzano.kmm_nasa_explorer.usecases.FindFavoriteUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.FindPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.GetFavoritesUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.GetPODUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODListUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.RequestPODSingleDayUseCase
import com.mariomanzano.kmm_nasa_explorer.usecases.SwitchItemToFavoriteUseCase
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun getBaseModules() = appModule + platformModule

val appModule = module {
    single<HttpClient> { httpClient() }
    single<Database> { Database(get()) }
    single<PODLocalDataSource> { PODRoomDataSource(get()) }
    singleOf(::DailyPicturesServiceImpl) { bind<DailyPicturesService>() }
    singleOf(::PODServerDataSource) { bind<PODRemoteDataSource>() }
    singleOf(::DailyPicturesRepository) { bind<DailyPicturesRepository>() }
    singleOf(::GetPODUseCase) { bind<GetPODUseCase>() }
    singleOf(::FindPODUseCase) { bind<FindPODUseCase>() }
    singleOf(::RequestPODListUseCase) { bind<RequestPODListUseCase>() }
    singleOf(::RequestPODSingleDayUseCase) { bind<RequestPODSingleDayUseCase>() }
    singleOf(::SwitchItemToFavoriteUseCase) { bind<SwitchItemToFavoriteUseCase>() }
    singleOf(::GetFavoritesUseCase) { bind<GetFavoritesUseCase>() }
    singleOf(::FindFavoriteUseCase) { bind<FindFavoriteUseCase>() }
    singleOf(::DailyPictureViewModel) { bind<DailyPictureViewModel>() }
    singleOf(::FavoriteViewModel) { bind<FavoriteViewModel>() }
    singleOf(::FavoriteDetailViewModel) { bind<FavoriteDetailViewModel>() }
}