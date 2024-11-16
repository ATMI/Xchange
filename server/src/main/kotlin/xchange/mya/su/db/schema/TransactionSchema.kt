package xchange.mya.su.db.schema

import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import xchange.mya.su.db.entity.ClientTable
import xchange.mya.su.db.entity.CurrencyTable
import xchange.mya.su.db.entity.CurrencyTable.symbol
import xchange.mya.su.db.entity.TransactionTable
import xchange.mya.su.db.entity.TransactionTable.amount
import xchange.mya.su.db.entity.TransactionTable.currency
import xchange.mya.su.db.entity.TransactionTable.recipient
import xchange.mya.su.db.entity.TransactionTable.sender
import xchange.mya.su.request.TransactionAck
import xchange.mya.su.response.CurrencyBalance
import xchange.mya.su.response.TransactionRecord
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

	suspend fun history(): List<TransactionRecord> = dbQuery {
		TransactionTable
			.join(CurrencyTable, JoinType.INNER, currency, CurrencyTable.id)
			.select(TransactionTable.id, sender, recipient, symbol, amount)
			.orderBy(TransactionTable.id, SortOrder.DESC)
			.limit(100)
			.map { row ->
				TransactionRecord(
					row[TransactionTable.id].value,
					row[sender],
					row[recipient],
					row[symbol],
					row[amount]
				)
			}
	}

	suspend fun balance(client: Long): List<CurrencyBalance> = dbQuery {
		val items = mutableListOf<CurrencyBalance>()
		exec(
			// TODO: group by: too many arguments
			stmt = """
			SELECT "currency".symbol, "select_balance"(?, "currency"."id")
			FROM "transaction"
		 		JOIN "currency" ON "transaction"."currency" = "currency"."id"
			GROUP BY "currency".symbol, "currency"."id";
			""".trimIndent(),

			args = listOf(
				LongColumnType() to client
			),
		) { row ->
			while (row.next()) {
				val currency = row.getString(1)
				val amount = row.getLong(2)
				items += CurrencyBalance(currency, amount)
			}
		}
		items
	}
}