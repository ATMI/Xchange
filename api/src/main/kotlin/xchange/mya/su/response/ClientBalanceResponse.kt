package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class BalanceItem(
	val currency: String,
	val amount: Long,
)

@Serializable
data class ClientBalanceResponse(
	val currencies: List<BalanceItem>,
)