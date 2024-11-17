package xchange.mya.su.entity

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import xchange.mya.su.signature.TransactionSigner

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
		signature = TransactionSigner.sign(key, id, sender, recipient, currency, amount, timestamp)
	}
}