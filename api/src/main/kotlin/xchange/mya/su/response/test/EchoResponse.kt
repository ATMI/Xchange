package xchange.mya.su.response.test

import kotlinx.serialization.Serializable

@Serializable
data class EchoResponse(
	val message: Int,
)