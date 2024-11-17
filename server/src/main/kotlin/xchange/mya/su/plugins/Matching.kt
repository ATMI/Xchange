package xchange.mya.su.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.batchInsert
import xchange.mya.su.db.entity.OrderTransactionTable
import xchange.mya.su.db.entity.OrderTransactionTable.order
import xchange.mya.su.db.entity.OrderTransactionTable.transaction
import xchange.mya.su.db.entity.TransactionTable
import xchange.mya.su.db.entity.TransactionTable.amount
import xchange.mya.su.db.entity.TransactionTable.currency
import xchange.mya.su.db.entity.TransactionTable.recipient
import xchange.mya.su.db.entity.TransactionTable.sender
import xchange.mya.su.db.entity.TransactionTable.signature
import xchange.mya.su.db.entity.TransactionTable.timestamp
import xchange.mya.su.db.schema.dbQuery
import xchange.mya.su.signature.TransactionSigner
import java.io.File
import java.util.*
import kotlin.math.min

private data class Demand(
	val id: Long,
	val client: Long,
	var amount: Long,
	val rate: Long,
) {
	val baseAmount = amount * rate / 100
}

private data class Mint(
	val order: Long,
	val client: Long,
	val currency: Long,
	val amount: Long,
	val timestamp: Instant,
)

private data class SignedMint(
	val id: Long,
	val mint: Mint,
	val signature: ByteArray,
) {
	constructor(key: Ed25519PrivateKeyParameters, id: Long, mint: Mint) : this(
		id,
		mint,
		TransactionSigner.sign(
			key,
			id,
			0,
			mint.client,
			mint.currency,
			mint.amount,
			mint.timestamp.toEpochMilliseconds(),
		)!!
	)
}

private suspend fun selectCurrencies(): List<Pair<Long, Long>>? = dbQuery {
	exec(
		stmt = """
			SELECT DISTINCT LEAST("base", "quote"), GREATEST("base", "quote")
			FROM "order";
		""".trimIndent()
	) { row ->
		generateSequence { if (row.next()) row else null }
			.map { row.getLong(1) to row.getLong(2) }
			.toMutableList()
	}
}

private fun org.jetbrains.exposed.sql.Transaction.selectBuyers(
	base: Long,
	quote: Long,
): MutableList<Demand>? {
	return exec(
		stmt = """
			SELECT "id", "client", "amount", "rate" 
			FROM "order_buy"(?, ?) 
			LIMIT 1000;
		""".trimMargin(),
		args = arrayListOf(
			LongColumnType() to base,
			LongColumnType() to quote,
		)
	) { row ->
		generateSequence { if (row.next()) row else null }
			.map {
				Demand(
					row.getLong(1),
					row.getLong(2),
					row.getLong(3),
					row.getLong(4),
				)
			}
			.toMutableList()
	}
}

private suspend fun performTransactions(transactions: List<SignedMint>) = dbQuery {
	TransactionTable.batchInsert(transactions) { s ->
		this[TransactionTable.id] = s.id
		this[sender] = 0
		this[recipient] = s.mint.client
		this[currency] = s.mint.currency
		this[amount] = s.mint.amount
		this[timestamp] = s.mint.timestamp
		this[signature] = s.signature
	}

	OrderTransactionTable.batchInsert(transactions) { s ->
		this[transaction] = s.id
		this[order] = s.mint.order
	}
}

private suspend fun reserveIds(n: Int) = dbQuery {
	exec(
		stmt = """
			SELECT setval('transaction_id_seq', nextval('transaction_id_seq') + ?);
		""",
		args = arrayListOf(
			LongColumnType() to n,
		)
	) { row ->
		row.next()
		row.getLong(1)
	}?.minus(n)
}

private suspend fun matchCurrency(
	key: Ed25519PrivateKeyParameters,
	base: Long,
	quote: Long,
) {
	val (buyers, sellers) = dbQuery {
		selectBuyers(base, quote) to selectBuyers(quote, base)
	}

	if (buyers.isNullOrEmpty() || sellers.isNullOrEmpty()) {
		return
	}

	val time = Clock.System.now()
	val n = 2 * min(buyers.size, sellers.size)
	val transactions = ArrayList<Mint>(n)

	while (buyers.isNotEmpty() && sellers.isNotEmpty()) {
		val buy = buyers.first()
		val sell = sellers.first()

		// buy.rate / 100 < 1 / (sell.rate / 100)
		if (buy.rate * sell.rate < 100 * 100) {
			break
		}

		val buyQuote = min(buy.amount, sell.baseAmount)
		val sellQuote = min(sell.amount, buy.baseAmount)

		buy.amount -= buyQuote
		sell.amount -= sellQuote

		transactions += Mint(buy.id, buy.client, quote, buyQuote, time)
		transactions += Mint(sell.id, sell.client, base, sellQuote, time)

		// todo: remove satisfied orders
		if (buy.amount == 0L) buyers.removeFirst()
		if (sell.amount == 0L) sellers.removeFirst()
	}

	val firstId = reserveIds(transactions.size)!!
	val signed = transactions.mapIndexed { i, t ->
		SignedMint(key, firstId + i, t)
	}
	performTransactions(signed)
}

private suspend fun Application.loadKey() = withContext(Dispatchers.IO) {
	val keyPath = environment.config.property("key.path").getString()
	val keyFile = File(keyPath)
	val key = Ed25519PrivateKeyParameters(keyFile.inputStream())
	key
}

fun Application.configureMatching() {
	GlobalScope.launch(Dispatchers.IO) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
		val key = loadKey()

		while (true) {
			delay(1000)

			val currencies = selectCurrencies()
			currencies?.forEach { pair ->
				matchCurrency(key, pair.first, pair.second)
			}
		}
	}
}