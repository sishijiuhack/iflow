package com.sishijiuhack.iflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyExpressionTest {
    @Test
    fun evaluateCents_supportsAdditionAndSubtraction() {
        assertEquals(12_030L, MoneyExpression.evaluateCents("100+20.50-0.20"))
    }

    @Test
    fun evaluateCents_supportsMultiplicationAndDivision() {
        assertEquals(1_500L, MoneyExpression.evaluateCents("10×3÷2"))
    }

    @Test
    fun evaluateCents_rejectsIncompleteOrNonPositiveExpressions() {
        assertNull(MoneyExpression.evaluateCents("10+"))
        assertNull(MoneyExpression.evaluateCents("10-10"))
        assertNull(MoneyExpression.evaluateCents("10÷0"))
    }

    @Test
    fun isPotential_acceptsExpressionsWhileTyping() {
        assertTrue(MoneyExpression.isPotential("12+"))
        assertTrue(MoneyExpression.isPotential("12.3×4"))
        assertFalse(MoneyExpression.isPotential("12++"))
        assertFalse(MoneyExpression.isPotential("12a"))
    }
}
