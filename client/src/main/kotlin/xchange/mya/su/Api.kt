package xchange.mya.su

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.protobuf.*
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import xchange.mya.su.request.client.ClientRegisterRequest
import xchange.mya.su.response.client.ClientRegisterResponse

class Api {
	private val http = HttpClient(CIO) {
		install(ContentNegotiation) {
			protobuf()
		}
	}

	suspend fun register(publicKey: Ed25519PublicKeyParameters): Long {
		val request = ClientRegisterRequest(publicKey)
		val response = http.post("http://localhost:8080/client/register") {
			contentType(ContentType.Application.ProtoBuf)
			setBody(request)
		}

		val message = response.body<ClientRegisterResponse>()
		return message.id
	}
}