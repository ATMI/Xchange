package xchange.mya.su.db.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TransactionTable : LongIdTable("transaction") {
	val sender = long("sender")
		.references(ClientTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
		.index()

	val recipient = long("recipient")
		.references(ClientTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
		.index()
		.check {
			it.neq(sender)
		}

	val currency = long("currency")
		.references(CurrencyTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
		.index()

	val amount = long("amount")
		.check {
			it.greater(0)
		}

	val timestamp = timestamp("timestamp")
	val signature = binary("signature", 64)
}

class Transaction(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Transaction>(TransactionTable)

	var sender by TransactionTable.sender
	var recipient by TransactionTable.recipient
	var currency by TransactionTable.currency
	var amount by TransactionTable.amount
	var timestamp by TransactionTable.timestamp
	var signature by TransactionTable.signature
}
