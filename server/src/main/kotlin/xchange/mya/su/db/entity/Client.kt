package xchange.mya.su.db.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ClientTable : LongIdTable("client") {
	val publicKey = binary("public_key", 32)
	val registered = timestamp("registered").defaultExpression(CurrentTimestamp)
}

class Client(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Client>(ClientTable)

	var publicKey by ClientTable.publicKey
	var registered by ClientTable.registered
}
