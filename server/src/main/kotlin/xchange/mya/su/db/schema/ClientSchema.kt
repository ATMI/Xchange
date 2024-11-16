package xchange.mya.su.db.schema

import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import xchange.mya.su.db.entity.ClientTable

class ClientSchema(database: Database) {

	init {
		transaction(database) {
			SchemaUtils.create(ClientTable)
		}
	}

	suspend fun insert(key: Ed25519PublicKeyParameters): EntityID<Long> {
		val encoded = key.encoded
		return dbQuery {
			ClientTable.insert {
				it[publicKey] = encoded
			}[ClientTable.id]
		}
	}
}