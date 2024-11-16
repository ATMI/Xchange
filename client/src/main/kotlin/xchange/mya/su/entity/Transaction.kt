package xchange.mya.su.entity

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer

data class Transaction(
	val id: Long,
	val sender: Long,
	val recipient: Long,
	val currency: Long,
	val amount: Long,
	val timestamp: Long,
	var signature: ByteArray? = null,
) {
	fun sign(key: Ed25519PrivateKeyParameters) {
		val str = "[$id, $timestamp] $sender -> $recipient: $amount $$currency"
		val data = str.toByteArray()

		val signer = Ed25519Signer()
		signer.init(true, key)
		signer.update(data, 0, data.size)

		signature = signer.generateSignature()
	}
}