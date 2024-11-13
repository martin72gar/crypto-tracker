package site.martinspace.cryptotracker.crypto.presentation.coin_list

import site.martinspace.cryptotracker.core.domain.util.NetworkError

sealed interface CoinListEvent {
    data class Error(val error: NetworkError) : CoinListEvent
}