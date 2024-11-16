package xchange.mya.su.response

import kotlinx.serialization.Serializable

@Serializable
data class CurrencySymbol(
	val id: Long,
	val symbol: String,
) {
	override fun toString(): String {
		return symbol
	}
}