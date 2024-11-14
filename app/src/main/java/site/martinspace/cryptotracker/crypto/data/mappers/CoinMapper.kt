package site.martinspace.cryptotracker.crypto.data.mappers

import android.os.Build
import androidx.annotation.RequiresApi
import site.martinspace.cryptotracker.crypto.data.networking.dto.CoinDto
import site.martinspace.cryptotracker.crypto.data.networking.dto.CoinPriceDto
import site.martinspace.cryptotracker.crypto.domain.Coin
import site.martinspace.cryptotracker.crypto.domain.CoinPrice
import java.time.Instant
import java.time.ZoneId

fun CoinDto.toCoin(): Coin {
    return Coin(
        id = id,
        rank = rank,
        name = name,
        symbol = symbol,
        marketCapUsd = marketCapUsd,
        priceUsd = priceUsd,
        changePercent24Hr = changePercent24Hr
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun CoinPriceDto.toCoinPrice(): CoinPrice {
    return CoinPrice(
        priceUsd = priceUsd,
        dateTime = Instant.ofEpochMilli(time).atZone(ZoneId.of("UTC"))
    )
}