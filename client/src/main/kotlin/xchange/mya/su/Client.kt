package xchange.mya.su

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.protobuf.*
import kotlinx.coroutines.runBlocking
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import xchange.mya.su.request.client.ClientRegisterRequest
import xchange.mya.su.response.client.ClientRegisterResponse
import java.security.SecureRandom

fun main() = runBlocking {
	println("Hello World!")

	val client = HttpClient(CIO) {
		install(ContentNegotiation) {
			protobuf()
		}
	}

	val random = SecureRandom()
	val generator = Ed25519KeyPairGenerator()
	val parameters = Ed25519KeyGenerationParameters(random)

	generator.init(parameters)
	val keyPair = generator.generateKeyPair()
	val publicKey = keyPair.public as Ed25519PublicKeyParameters

	val request = ClientRegisterRequest(publicKey)
	val response = client.post("http://localhost:8080/client/register") {
		contentType(ContentType.Application.ProtoBuf)
		setBody(request)
	}

	val message = response.body<ClientRegisterResponse>()
	println(message)
}