package xchange.mya.su.db.schema

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import xchange.mya.su.db.entity.CurrencyTable
import xchange.mya.su.db.entity.CurrencyTable.symbol
import xchange.mya.su.response.CurrencySymbol

class CurrencySchema(database: Database) {
	init {
		transaction(database) {
			SchemaUtils.create(CurrencyTable)
		}
	}

	suspend fun list(): List<CurrencySymbol> = dbQuery {
		CurrencyTable
			.select(CurrencyTable.id, symbol)
			.map { row ->
				CurrencySymbol(
					row[CurrencyTable.id].value,
					row[symbol],
				)
			}
	}
}