package com.sishijiuhack.iflow.core.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@JvmInline
value class MoneyCents(val cents: Long) {
    fun format(locale: Locale = Locale.CHINA): String {
        return NumberFormat.getCurrencyInstance(locale).format(BigDecimal.valueOf(cents, 2))
    }
}
