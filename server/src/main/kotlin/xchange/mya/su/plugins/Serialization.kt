package xchange.mya.su.plugins

import io.ktor.serialization.kotlinx.protobuf.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*


fun Application.configureSerialization() {
	install(ContentNegotiation) {
		protobuf()
	}
}
