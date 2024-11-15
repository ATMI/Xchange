package xchange.mya.su.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import xchange.mya.su.db.schema.ClientSchema
import xchange.mya.su.db.schema.TransactionSchema
import xchange.mya.su.request.ClientRegisterRequest
import xchange.mya.su.request.TransactionAck
import xchange.mya.su.response.ClientBalanceResponse
import xchange.mya.su.response.ClientRegisterResponse
import xchange.mya.su.response.TransactionHistoryResponse
import xchange.mya.su.response.TransactionSynAck

fun Application.configureDatabase() {
	val database = connectToPostgres()

	val clientSchema = ClientSchema(database)
	val transactionSchema = TransactionSchema(database)

	routing {
		post("/client/register") {
			val request = call.receive<ClientRegisterRequest>()
			val clientId = clientSchema.insert(request.key)
			val response = ClientRegisterResponse(clientId.value)
			call.respond(HttpStatusCode.Created, response)
		}

		get("/client/balance/{id}") {
			val id = call.parameters["id"]!!.toLong()
			val balance = transactionSchema.balance(id)
			val response = ClientBalanceResponse(balance)
			call.respond(HttpStatusCode.OK, response)
		}
	}

	routing {
		get("/transaction/syn") {
			val result = transactionSchema.syn()
			if (result == null) {
				call.respond(HttpStatusCode.NotFound)
			} else {
				val response = TransactionSynAck(result.first, result.second.time)
				call.respond(HttpStatusCode.OK, response)
			}
		}

		post("/transaction/ack") {
			val request = call.receive<TransactionAck>()
			transactionSchema.ack(request)
			call.respond(HttpStatusCode.OK)
		}

		get("/transaction/history") {
			val transactions = transactionSchema.history()
			val response = TransactionHistoryResponse(transactions)
			call.respond(HttpStatusCode.OK, response)
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
