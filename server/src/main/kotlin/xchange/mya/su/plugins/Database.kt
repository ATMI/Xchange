package xchange.mya.su.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import xchange.mya.su.db.schema.ClientSchema
import xchange.mya.su.db.schema.TransactionSchema
import xchange.mya.su.request.client.ClientRegisterRequest
import xchange.mya.su.request.transaction.TransactionAck
import xchange.mya.su.response.client.ClientRegisterResponse
import xchange.mya.su.response.transaction.TransactionSynAck

fun Application.configureDatabase() {
	val database = connectToPostgres()

	val clientSchema = ClientSchema(database)
	routing {
		post("/client/register") {
			val request = call.receive<ClientRegisterRequest>()
			val clientId = clientSchema.insert(request.key)
			val response = ClientRegisterResponse(clientId.value)
			call.respond(HttpStatusCode.Created, response)
		}
	}

	val transactionSchema = TransactionSchema(database)
	routing {
		get("transaction/syn") {
			val result = transactionSchema.syn()
			if (result == null) {
				call.respond(HttpStatusCode.NotFound)
			} else {
				val response = TransactionSynAck(result.first, result.second.time)
				call.respond(HttpStatusCode.OK, response)
			}
		}

		post("transaction/ack") {
			val request = call.receive<TransactionAck>()
			transactionSchema.ack(request)
			call.respond(HttpStatusCode.OK)
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
