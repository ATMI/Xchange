package xchange.mya.su.request

import kotlinx.serialization.Serializable

@Serializable
data class TransactionAck(
	val id: Long,
	val sender: Long,
	val recipient: Long,
	val currency: Long,
	val amount: Long,
	val timestamp: Long,
	val signature: ByteArray,
)