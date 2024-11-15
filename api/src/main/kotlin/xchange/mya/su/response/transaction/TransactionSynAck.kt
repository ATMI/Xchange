package xchange.mya.su.response.transaction

import kotlinx.serialization.Serializable

@Serializable
data class TransactionSynAck(
	val id: Long,
	val timestamp: Long,
)
