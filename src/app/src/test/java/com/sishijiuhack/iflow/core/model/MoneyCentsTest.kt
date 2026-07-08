package com.sishijiuhack.iflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class MoneyCentsTest {
    @Test
    fun format_usesIntegerCentsWithoutLosingPrecision() {
        assertEquals("¥0.01", MoneyCents(1L).format(Locale.CHINA))
        assertEquals("¥1,234,567.89", MoneyCents(123_456_789L).format(Locale.CHINA))
    }
}
