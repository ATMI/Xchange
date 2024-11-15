package xchange.mya.su.request.client

import kotlinx.serialization.Serializable
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import xchange.mya.su.serializer.Ed25519PublicKeySerializer

@Serializable
data class ClientRegisterRequest(
	@Serializable(with = Ed25519PublicKeySerializer::class)
	val key: Ed25519PublicKeyParameters
)
