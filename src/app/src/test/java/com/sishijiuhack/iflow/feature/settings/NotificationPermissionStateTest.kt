package com.sishijiuhack.iflow.feature.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionStateTest {
    @Test
    fun containsEnabledNotificationListener_matchesTrimmedCaseInsensitiveEntries() {
        val flatName = "com.sishijiuhack.iflow/.notification.PaymentNotificationListenerService"
        val enabledListeners = listOf(
            "com.example.other/.Listener",
            "  COM.SISHIJIUHACK.IFLOW/.NOTIFICATION.PAYMENTNOTIFICATIONLISTENERSERVICE  ",
        ).joinToString(":")

        assertTrue(containsEnabledNotificationListener(enabledListeners, flatName))
    }

    @Test
    fun containsEnabledNotificationListener_returnsFalseWhenEntryIsMissing() {
        val flatName = "com.sishijiuhack.iflow/.notification.PaymentNotificationListenerService"
        val enabledListeners = "com.example.other/.Listener:com.example.second/.Listener"

        assertFalse(containsEnabledNotificationListener(enabledListeners, flatName))
    }
}
