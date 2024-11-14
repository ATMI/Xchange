package xchange.mya.su

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import xchange.mya.su.plugins.*

fun main() {
	embeddedServer(
		factory = Netty,
		port = 8080,
		host = "0.0.0.0",
		module = Application::module
	).start(wait = true)
}

fun Application.module() {
	configureSerialization()
//	configureDatabase()
	configureRouting()
}
