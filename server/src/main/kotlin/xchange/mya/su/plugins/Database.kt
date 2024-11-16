package xchange.mya.su.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.jetbrains.exposed.sql.Database
import xchange.mya.su.db.schema.ClientSchema
import xchange.mya.su.db.schema.CurrencySchema
import xchange.mya.su.db.schema.OrderSchema
import xchange.mya.su.db.schema.TransactionSchema
import xchange.mya.su.request.OrderRequest
import xchange.mya.su.request.TransactionAck
import xchange.mya.su.response.TransactionSynAck

fun Application.configureDatabase() {
	val database = connectToPostgres()

	val clientSchema = ClientSchema(database)
	val currencySchema = CurrencySchema(database)
	val transactionSchema = TransactionSchema(database)
	val orderSchema = OrderSchema(database)

	routing {
		post("/client/register") {
			val stream = call.receiveStream()
			val key = Ed25519PublicKeyParameters(stream)
			val clientId = clientSchema.insert(key)
			call.respond(HttpStatusCode.Created, clientId.value)
		}

		get("/client/balance/{id}") {
			val id = call.parameters["id"]!!.toLong()
			val balance = transactionSchema.balance(id)
			call.respond(HttpStatusCode.OK, balance)
		}
	}

	routing {
		get("/currency/list") {
			val currencies = currencySchema.list()
			call.respond(currencies)
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
			call.respond(HttpStatusCode.OK, transactions)
		}
	}

	routing {
		post("/order/create") {
			val order = call.receive<OrderRequest>()
			orderSchema.insert(order)
			call.respond(HttpStatusCode.Created)
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
