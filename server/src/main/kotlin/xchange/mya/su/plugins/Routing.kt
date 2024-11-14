package xchange.mya.su.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xchange.mya.su.request.test.EchoRequest
import xchange.mya.su.response.test.EchoResponse

fun Application.configureRouting() {
	routing {
		post("/echo") {
			val request = call.receive<EchoRequest>()
			val response = EchoResponse(request.message)
			call.respond(HttpStatusCode.OK, response)
		}
	}
}
