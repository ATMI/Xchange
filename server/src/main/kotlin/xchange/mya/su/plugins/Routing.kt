package xchange.mya.su.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
	routing {
		post("/echo") {
			val request = call.receive<String>()
			call.respond(HttpStatusCode.OK, request)
		}
	}
}
