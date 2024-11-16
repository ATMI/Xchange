package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionRecord(
	val id: Long,
	val sender: Long,
	val recipient: Long,
	val currency: String,
	val amount: Long,
)
