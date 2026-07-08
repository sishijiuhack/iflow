package com.sishijiuhack.iflow.core.model

object MoneyParser {
    private val validAmount = Regex("""^\d{0,9}(\.\d{0,2})?$""")

    fun isPotentialAmount(input: String): Boolean {
        val normalized = input.trim()
        return normalized.isNotEmpty() && validAmount.matches(normalized)
    }

    fun parseCents(input: String): Long? {
        val normalized = input.trim()
        if (!isPotentialAmount(normalized)) return null
        if (normalized.none(Char::isDigit)) return null
        val parts = normalized.split(".")
        val yuan = parts.getOrNull(0)?.takeIf { it.isNotEmpty() }?.toLongOrNull() ?: 0L
        val cents = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2).toLongOrNull() ?: 0L
        return yuan * 100 + cents
    }

    fun formatInput(cents: Long): String {
        val yuan = cents / 100
        val centPart = (cents % 100).toString().padStart(2, '0')
        return "$yuan.$centPart"
    }
}
