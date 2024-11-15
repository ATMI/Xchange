package xchange.mya.su

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.protobuf.*
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import xchange.mya.su.entity.Transaction
import xchange.mya.su.request.client.ClientRegisterRequest
import xchange.mya.su.request.transaction.TransactionAck
import xchange.mya.su.response.client.ClientRegisterResponse
import xchange.mya.su.response.transaction.TransactionSynAck

class Api {
	private val http = HttpClient(CIO) {
		install(ContentNegotiation) {
			protobuf()
		}
	}

	suspend fun register(key: Ed25519PublicKeyParameters): ClientRegisterResponse {
		val request = ClientRegisterRequest(key)
		val response = http.post("http://localhost:8080/client/register") {
			contentType(ContentType.Application.ProtoBuf)
			setBody(request)
		}

		val body = response.body<ClientRegisterResponse>()
		return body
	}

	suspend fun transactionSyn(): TransactionSynAck {
		val response = http.get("http://localhost:8080/transaction/syn")
		val body = response.body<TransactionSynAck>()
		return body
	}

	suspend fun transactionAck(transaction: Transaction) {
		val ack = TransactionAck(
			transaction.id,
			transaction.sender,
			transaction.recipient,
			transaction.currency,
			transaction.amount,
			transaction.timestamp,
			transaction.signature!!,
		)

		http.post("http://localhost:8080/transaction/ack") {
			contentType(ContentType.Application.ProtoBuf)
			setBody(ack)
		}
	}
}