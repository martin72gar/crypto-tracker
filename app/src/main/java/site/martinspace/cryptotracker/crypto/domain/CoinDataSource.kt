package site.martinspace.cryptotracker.crypto.domain

import site.martinspace.cryptotracker.core.domain.util.NetworkError
import site.martinspace.cryptotracker.core.domain.util.Result

interface CoinDataSource {
    suspend fun getCoins(): Result<List<Coin>, NetworkError>
}