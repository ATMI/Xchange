package xchange.mya.su.db.schema

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import xchange.mya.su.db.entity.ClientTable
import xchange.mya.su.db.entity.CurrencyTable
import xchange.mya.su.db.entity.TransactionTable
import xchange.mya.su.request.transaction.TransactionAck
import java.sql.Timestamp

class TransactionSchema(database: Database) {

	init {
		transaction(database) {
			SchemaUtils.create(ClientTable)
			SchemaUtils.create(CurrencyTable)
			SchemaUtils.create(TransactionTable)
		}
	}

	suspend fun ack(ack: TransactionAck) {
		val instant = Instant.fromEpochMilliseconds(ack.timestamp)
		return dbQuery {
			TransactionTable.insert {
				it[id] = ack.id
				it[sender] = ack.sender
				it[recipient] = ack.recipient
				it[currency] = ack.currency
				it[amount] = ack.amount
				it[timestamp] = instant
				it[signature] = ack.signature
			}
		}
	}

	suspend fun syn(): Pair<Long, Timestamp>? {
		return dbQuery {
			exec("SELECT nextval('transaction_id_seq'), CURRENT_TIMESTAMP;") { result ->
				result.next()

				val id = result.getLong(1)
				val timestamp = result.getTimestamp(2)
				id to timestamp
			}
		}
	}

	private suspend fun <T> dbQuery(block: suspend org.jetbrains.exposed.sql.Transaction.() -> T): T =
		newSuspendedTransaction(Dispatchers.IO) {
			block()
		}
}