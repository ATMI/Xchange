package xchange.mya.su.signature

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

object TransactionSigner {
	fun sign(
		key: Ed25519PrivateKeyParameters,
		id: Long,
		sender: Long,
		recipient: Long,
		currency: Long,
		amount: Long,
		timestamp: Long,
	): ByteArray? {
		val str = "[$id] $sender -> $recipient: $amount $$currency"
		val data = str.toByteArray()

		val signer = Ed25519Signer()
		signer.init(true, key)
		signer.update(data, 0, data.size)

		return signer.generateSignature()
	}
}