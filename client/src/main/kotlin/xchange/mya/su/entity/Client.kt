package xchange.mya.su.entity

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters

data class Client(
	val id: Long,
	val privateKey: Ed25519PrivateKeyParameters,
	val publicKey: Ed25519PublicKeyParameters,
)
