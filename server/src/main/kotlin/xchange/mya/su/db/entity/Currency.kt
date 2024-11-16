package xchange.mya.su.db.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object CurrencyTable : LongIdTable() {
	var name = varchar("name", 64)
	var symbol = varchar("symbol", 4)
		.uniqueIndex()
}

class Currency(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Currency>(CurrencyTable)

	var name by CurrencyTable.name
	var symbol by CurrencyTable.symbol
}