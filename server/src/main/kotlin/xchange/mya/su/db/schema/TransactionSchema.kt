package xchange.mya.su.db.schema

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import xchange.mya.su.db.entity.ClientTable
import xchange.mya.su.db.entity.CurrencyTable
import xchange.mya.su.db.entity.CurrencyTable.code
import xchange.mya.su.db.entity.TransactionTable
import xchange.mya.su.db.entity.TransactionTable.amount
import xchange.mya.su.db.entity.TransactionTable.currency
import xchange.mya.su.db.entity.TransactionTable.recipient
import xchange.mya.su.db.entity.TransactionTable.sender
import xchange.mya.su.request.TransactionAck
import xchange.mya.su.response.BalanceItem
import xchange.mya.su.response.TransactionHistoryItem
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

	suspend fun syn(): Pair<Long, Timestamp>? = dbQuery {
		exec("SELECT nextval('transaction_id_seq'), CURRENT_TIMESTAMP;") { result ->
			result.next()

			val id = result.getLong(1)
			val timestamp = result.getTimestamp(2)
			id to timestamp
		}
	}

	suspend fun history(): List<TransactionHistoryItem> = dbQuery {
		TransactionTable
			.join(CurrencyTable, JoinType.INNER, currency, CurrencyTable.id)
			.select(TransactionTable.id, sender, recipient, code, amount)
			.limit(10)
			.map { row ->
				TransactionHistoryItem(
					row[TransactionTable.id].value,
					row[sender],
					row[recipient],
					row[code],
					row[amount]
				)
			}
	}

	suspend fun balance(client: Long): List<BalanceItem> = dbQuery {
		val items = mutableListOf<BalanceItem>()
		exec(
			// TODO: group by: too many arguments
			stmt = """
			SELECT "currency"."code", "select_balance"(?, "currency"."id")
			FROM "transaction"
		 		JOIN "currency" ON "transaction"."currency" = "currency"."id"
			GROUP BY "currency"."code", "currency"."id";
			""".trimIndent(),

			args = listOf(
				LongColumnType() to client
			),
		) { row ->
			while (row.next()) {
				val currency = row.getString(1)
				val amount = row.getLong(2)
				items += BalanceItem(currency, amount)
			}
		}
		items
	}


	private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
		newSuspendedTransaction(Dispatchers.IO) {
			block()
		}
}