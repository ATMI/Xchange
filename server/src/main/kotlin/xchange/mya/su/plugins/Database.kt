package xchange.mya.su.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import xchange.mya.su.db.schema.ClientSchema
import xchange.mya.su.request.client.ClientRegisterRequest
import xchange.mya.su.response.client.ClientRegisterResponse

fun Application.configureDatabase() {
	val database = connectToPostgres()
	val clientSchema = ClientSchema(database)

	routing {
		post("/client/register") {
			val request = call.receive<ClientRegisterRequest>()
			val clientId = clientSchema.insert(request.publicKey)
			val response = ClientRegisterResponse(clientId.value)
			call.respond(HttpStatusCode.Created, response)
		}
	}
}

fun Application.connectToPostgres(): Database {
	Class.forName("org.postgresql.Driver")
	val url = environment.config.property("postgres.url").getString()
	val user = environment.config.property("postgres.user").getString()
	val password = environment.config.property("postgres.password").getString()

	return Database.connect(
		url = url,
		user = user,
		password = password,
	)
}
