package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class TransactionSynAck(
	val id: Long,
	val timestamp: Long,
)
