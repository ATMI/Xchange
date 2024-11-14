package xchange.mya.su

import io.ktor.server.application.*
import io.ktor.server.netty.*
import xchange.mya.su.plugins.configureDatabase
import xchange.mya.su.plugins.configureRouting
import xchange.mya.su.plugins.configureSerialization

fun main(args: Array<String>) {
	EngineMain.main(args)
}

fun Application.module() {
	configureSerialization()
	configureDatabase()
	configureRouting()
}
