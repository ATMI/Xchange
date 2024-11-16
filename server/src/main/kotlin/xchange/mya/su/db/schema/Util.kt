package xchange.mya.su.db.schema

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
	newSuspendedTransaction(Dispatchers.IO) {
		block()
	}
