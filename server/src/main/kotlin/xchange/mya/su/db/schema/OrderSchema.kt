package xchange.mya.su.db.schema

import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import xchange.mya.su.db.entity.OrderTable
import xchange.mya.su.db.entity.OrderTransactionTable
import xchange.mya.su.db.entity.TransactionTable
import xchange.mya.su.request.OrderRequest

class OrderSchema(database: Database) {
	init {
		transaction(database) {
			SchemaUtils.create(OrderTable)
		}
	}

	suspend fun insert(orderReq: OrderRequest) = dbQuery {
		TransactionTable.insert {
			it[id] = orderReq.transaction
			it[sender] = orderReq.client
			it[recipient] = 0
			it[currency] = orderReq.base
			it[amount] = orderReq.amount * orderReq.rate / 100
			it[timestamp] = Instant.fromEpochMilliseconds(orderReq.timestamp)
			it[signature] = orderReq.signature
		}

		val id = OrderTable.insert {
			it[client] = orderReq.client
			it[amount] = orderReq.amount
			it[base] = orderReq.base
			it[quote] = orderReq.quote
			it[rate] = orderReq.rate
		}[OrderTable.id]

		OrderTransactionTable.insert {
			it[order] = id.value
			it[transaction] = orderReq.transaction
		}
	}
}