package com.sishijiuhack.iflow.core.model

object MoneyExpression {
    private val operators = setOf('+', '-', '*', '/', 'x', 'X', '×', '÷')
    private val operatorSeparators = charArrayOf('+', '-', '*', '/', 'x', 'X', '×', '÷')

    fun isPotential(input: String): Boolean {
        val normalized = input.normalizedExpression()
        if (normalized.isBlank()) return true
        if (normalized.any { !it.isDigit() && it !in operators && it != '.' && it != ',' }) return false
        if (normalized.zipWithNext().any { (left, right) -> left in operators && right in operators }) return false
        return normalized.split(*operatorSeparators).all { part ->
            part.isBlank() || MoneyParser.isPotentialAmount(part)
        }
    }

    fun evaluateCents(input: String): Long? {
        val normalized = input.normalizedExpression()
            .replace('×', '*')
            .replace('x', '*')
            .replace('X', '*')
            .replace('÷', '/')
        if (normalized.isBlank()) return null
        val tokens = tokenize(normalized) ?: return null
        val values = mutableListOf<Long>()
        val expressionOperators = mutableListOf<Char>()
        tokens.forEach { token ->
            when (token) {
                "+", "-", "*", "/" -> expressionOperators += token.single()
                else -> values += MoneyParser.parseCents(token) ?: return null
            }
        }
        if (values.size != expressionOperators.size + 1) return null

        var index = 0
        while (index < expressionOperators.size) {
            val operator = expressionOperators[index]
            if (operator == '*' || operator == '/') {
                val left = values[index]
                val right = values[index + 1]
                val result = if (operator == '*') {
                    (left * right) / 100L
                } else {
                    if (right == 0L) return null
                    (left * 100L) / right
                }
                values[index] = result
                values.removeAt(index + 1)
                expressionOperators.removeAt(index)
            } else {
                index += 1
            }
        }

        val total = values.drop(1).foldIndexed(values.first()) { opIndex, acc, value ->
            if (expressionOperators[opIndex] == '+') acc + value else acc - value
        }
        return total.takeIf { it > 0L }
    }

    private fun tokenize(input: String): List<String>? {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        input.forEach { char ->
            if (char in setOf('+', '-', '*', '/')) {
                if (current.isEmpty()) return null
                tokens += current.toString()
                tokens += char.toString()
                current.clear()
            } else {
                current.append(char)
            }
        }
        if (current.isEmpty()) return null
        tokens += current.toString()
        return tokens
    }

    private fun String.normalizedExpression(): String = trim()
        .replace('＋', '+')
        .replace('－', '-')
        .replace('＊', '*')
        .replace('／', '/')
        .replace('，', ',')
        .replace('。', '.')
        .replace('．', '.')
        .filterNot { it.isWhitespace() || Character.isSpaceChar(it) }
}
