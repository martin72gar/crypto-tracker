package site.martinspace.cryptotracker.di

import io.ktor.client.engine.cio.CIO
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import site.martinspace.cryptotracker.core.data.networking.HttpClientFactory
import site.martinspace.cryptotracker.crypto.data.networking.RemoteCoinDataSource
import site.martinspace.cryptotracker.crypto.domain.CoinDataSource
import site.martinspace.cryptotracker.crypto.presentation.coin_list.CoinListViewModel

val appModule = module{
    single { HttpClientFactory.create(CIO.create()) }
    singleOf(::RemoteCoinDataSource).bind<CoinDataSource>()

    viewModelOf(::CoinListViewModel)
}