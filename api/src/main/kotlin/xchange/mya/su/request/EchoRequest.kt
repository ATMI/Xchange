package xchange.mya.su.request

import kotlinx.serialization.Serializable

@Serializable
data class EchoRequest(
	val message: Int,
)
