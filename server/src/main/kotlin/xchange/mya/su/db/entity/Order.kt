package xchange.mya.su.db.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

// all orders are buy
object OrderTable : LongIdTable("order") {
	val client = long("client")
		.references(ClientTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
		.index()

	val base = long("base")
		.references(CurrencyTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
		.index()

	val quote = long("quote")
		.references(CurrencyTable.id, ReferenceOption.RESTRICT, ReferenceOption.RESTRICT)
		.index()

	val amount = long("amount")
		.check {
			it.greater(0)
		}

	val rate = long("rate")
		.check {
			it.greater(0)
		}
}

class Order(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Order>(OrderTable)

	var client by OrderTable.client
	var base by OrderTable.base
	var quote by OrderTable.quote
	var amount by OrderTable.amount
	var rate by OrderTable.rate
}
