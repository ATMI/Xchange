package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyBalance(
	val currency: String,
	val amount: Long,
)
