package com.sishijiuhack.iflow.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryIconsTest {
    @Test
    fun toCategoryEmoji_mapsDefaultCategoryIcons() {
        assertEquals("🍽", "restaurant".toCategoryEmoji())
        assertEquals("✈", "flight".toCategoryEmoji())
        assertEquals("🧧", "card_giftcard".toCategoryEmoji())
    }

    @Test
    fun toCategoryEmoji_fallsBackForMissingOrUnknownIcons() {
        assertEquals("•", null.toCategoryEmoji())
        assertEquals("•", "long_unknown_icon".toCategoryEmoji())
    }
}
