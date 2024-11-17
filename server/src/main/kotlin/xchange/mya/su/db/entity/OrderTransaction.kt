package xchange.mya.su.db.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OrderTransactionTable : LongIdTable("order_transaction") {
	val order = long("order")
		.references(OrderTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)

	val transaction = long("transaction")
		.references(TransactionTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
}

class OrderTransaction(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<OrderTransaction>(OrderTransactionTable)

	var order by OrderTransactionTable.order
	var transaction by OrderTransactionTable.transaction
}