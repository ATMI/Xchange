package xchange.mya.su.db.entity

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object ClientTable : LongIdTable("client") {
	val publicKey = binary("public_key", Ed25519PublicKeyParameters.KEY_SIZE)
	val registered = datetime("registered")
		.defaultExpression(CurrentDateTime)
}

class Client(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Client>(ClientTable)

	var publicKey by ClientTable.publicKey
	var registered by ClientTable.registered
}
