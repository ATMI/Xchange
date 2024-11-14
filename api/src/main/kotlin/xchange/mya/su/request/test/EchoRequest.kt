package xchange.mya.su.request.test

import kotlinx.serialization.Serializable

@Serializable
data class EchoRequest(
	val message: Int,
)
