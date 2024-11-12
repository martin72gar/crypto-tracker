package site.martinspace.cryptotracker.crypto.presentation.coin_list

import site.martinspace.cryptotracker.crypto.presentation.models.CoinUi

sealed interface CoinListAction {
    data class OnCoinClick(val coinUi: CoinUi) : CoinListAction
}