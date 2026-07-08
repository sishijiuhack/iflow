package com.sishijiuhack.iflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyParserTest {
    @Test
    fun parseCents_convertsYuanInputToIntegerCents() {
        assertEquals(1200L, MoneyParser.parseCents("12"))
        assertEquals(1230L, MoneyParser.parseCents("12.3"))
        assertEquals(1234L, MoneyParser.parseCents("12.34"))
        assertEquals(1L, MoneyParser.parseCents(" 0.01 "))
    }

    @Test
    fun parseCents_acceptsFractionOnlyInput() {
        assertEquals(50L, MoneyParser.parseCents(".5"))
        assertEquals(5L, MoneyParser.parseCents(".05"))
    }

    @Test
    fun parseCents_rejectsInvalidAmounts() {
        assertNull(MoneyParser.parseCents(""))
        assertNull(MoneyParser.parseCents("."))
        assertNull(MoneyParser.parseCents("abc"))
        assertNull(MoneyParser.parseCents("12.345"))
        assertNull(MoneyParser.parseCents("1,200"))
    }

    @Test
    fun isPotentialAmount_allowsProgressiveValidInputOnly() {
        assertTrue(MoneyParser.isPotentialAmount("12."))
        assertTrue(MoneyParser.isPotentialAmount(".5"))
        assertFalse(MoneyParser.isPotentialAmount(""))
        assertFalse(MoneyParser.isPotentialAmount("12.345"))
        assertFalse(MoneyParser.isPotentialAmount("12a"))
    }

    @Test
    fun formatInput_usesTwoDecimalPlaces() {
        assertEquals("12.00", MoneyParser.formatInput(1200L))
        assertEquals("0.05", MoneyParser.formatInput(5L))
    }
}
