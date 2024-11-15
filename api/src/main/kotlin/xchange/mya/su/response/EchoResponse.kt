package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class EchoResponse(
	val message: Int,
)