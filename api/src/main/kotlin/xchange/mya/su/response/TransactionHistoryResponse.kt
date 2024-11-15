package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionHistoryItem(
	val id: Long,
	val sender: Long,
	val recipient: Long,
	val currency: String,
	val amount: Long,
)

@Serializable
data class TransactionHistoryResponse(
	val transactions: List<TransactionHistoryItem>,
)