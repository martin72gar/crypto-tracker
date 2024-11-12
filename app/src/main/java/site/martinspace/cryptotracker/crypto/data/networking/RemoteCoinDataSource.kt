package site.martinspace.cryptotracker.crypto.data.networking

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import site.martinspace.cryptotracker.core.data.networking.constructUrl
import site.martinspace.cryptotracker.core.data.networking.safeCall
import site.martinspace.cryptotracker.core.domain.util.NetworkError
import site.martinspace.cryptotracker.core.domain.util.Result
import site.martinspace.cryptotracker.core.domain.util.map
import site.martinspace.cryptotracker.crypto.data.mappers.toCoin
import site.martinspace.cryptotracker.crypto.data.networking.dto.CoinsResponseDto
import site.martinspace.cryptotracker.crypto.domain.Coin
import site.martinspace.cryptotracker.crypto.domain.CoinDataSource

class RemoteCoinDataSource(
    private val httpClient: HttpClient
) : CoinDataSource {
    override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
        return safeCall<CoinsResponseDto> {
            httpClient.get(
                urlString = constructUrl("/assets")
            )
        }.map { response ->
            response.data.map { it.toCoin() }
        }
    }
}