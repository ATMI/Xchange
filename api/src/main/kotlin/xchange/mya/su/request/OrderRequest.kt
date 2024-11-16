package xchange.mya.su.request

import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
	val transaction: Long,
	val client: Long,
	val base: Long,
	val quote: Long,
	val amount: Long,
	val rate: Long,
	val timestamp: Long,
	val signature: ByteArray,
)