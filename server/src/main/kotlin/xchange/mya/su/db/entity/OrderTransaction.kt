package xchange.mya.su.db.entity

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object OrderTransactionTable : Table("order_transaction") {
	val order = long("order")
		.references(OrderTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)

	val transaction = long("transaction")
		.references(TransactionTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)

	override val primaryKey: PrimaryKey
		get() = PrimaryKey(order, transaction)
}
