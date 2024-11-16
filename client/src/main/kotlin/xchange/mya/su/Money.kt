package xchange.mya.su

import java.util.regex.Pattern

object Money {
	const val PATTERN = "^(\\d*)\\.?(\\d{0,2})$"

	fun String.toMoney(): Long {
		val pattern = Pattern.compile(PATTERN)
		val match = pattern.matcher(this)
		if (!match.find()) {
			throw IllegalArgumentException("Invalid Money format: $this")
		}

		val wholeGroup = match.group(1)
		val centsGroup = match.group(2)

		val whole = wholeGroup.toLongOrNull() ?: 0
		var cents = centsGroup.toLongOrNull() ?: 0

		if (cents > 0 && centsGroup.length == 1) {
			cents *= 10
		}

		val amount = 100 * whole + cents
		return amount
	}
}