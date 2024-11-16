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
import xchange.mya.su.request.OrderRequest
import xchange.mya.su.request.TransactionAck
import xchange.mya.su.response.CurrencyBalance
import xchange.mya.su.response.CurrencySymbol
import xchange.mya.su.response.TransactionRecord
import xchange.mya.su.response.TransactionSynAck

class Api {
	private val http = HttpClient(CIO) {
		install(ContentNegotiation) {
			protobuf()
		}
	}

	suspend fun clientRegister(key: Ed25519PublicKeyParameters): Long {
		val response = http.post("http://localhost:8080/client/register") {
			contentType(ContentType.Application.OctetStream)
			setBody(key.encoded)
		}

		val body = response.body<Long>()
		return body
	}

	suspend fun clientBalance(id: Long): List<CurrencyBalance> {
		val response = http.get("http://localhost:8080/client/balance/$id")
		val body = response.body<List<CurrencyBalance>>()
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

	suspend fun transactionHistory(): List<TransactionRecord> {
		val response = http.get("http://localhost:8080/transaction/history")
		val body = response.body<List<TransactionRecord>>()
		return body
	}

	suspend fun currencyList(): List<CurrencySymbol> {
		val response = http.get("http://localhost:8080/currency/list")
		val body = response.body<List<CurrencySymbol>>()
		return body
	}

	suspend fun orderCreate(order: OrderRequest) {
		http.post("http://localhost:8080/order/create") {
			contentType(ContentType.Application.ProtoBuf)
			setBody(order)
		}
	}
}