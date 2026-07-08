package com.sishijiuhack.iflow.core.model

object MoneyParser {
    private val validAmount = Regex("""^\d{0,9}(\.\d{0,2})?$""")

    fun isPotentialAmount(input: String): Boolean {
        val normalized = input.normalizeAmountInput()
        return normalized.isNotEmpty() && validAmount.matches(normalized)
    }

    fun parseCents(input: String): Long? {
        val normalized = input.normalizeAmountInput()
        if (!isPotentialAmount(normalized)) return null
        if (normalized.none(Char::isDigit)) return null
        val parts = normalized.split(".")
        val yuan = parts.getOrNull(0)?.takeIf { it.isNotEmpty() }?.toLongOrNull() ?: 0L
        val cents = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2).toLongOrNull() ?: 0L
        return (yuan * 100 + cents).takeIf { it > 0L }
    }

    fun formatInput(cents: Long): String {
        val yuan = cents / 100
        val centPart = (cents % 100).toString().padStart(2, '0')
        return "$yuan.$centPart"
    }

    private fun String.normalizeAmountInput(): String {
        val normalized = trim().map { char ->
            when (char) {
                in '０'..'９' -> '0' + (char - '０')
                '．', '。' -> '.'
                else -> char
            }
        }.joinToString(separator = "")
        return normalized
            .replace(Regex("人民币|rmb|cny", RegexOption.IGNORE_CASE), "")
            .filterNot { it == '¥' || it == '￥' || it == '元' || it == '整' || it.isWhitespace() }
    }
}
