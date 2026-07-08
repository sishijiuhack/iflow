package com.sishijiuhack.iflow.feature.settings

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.sishijiuhack.iflow.notification.PaymentNotificationListenerService

fun isNotificationListenerEnabled(context: Context): Boolean {
    val flatName = ComponentName(context, PaymentNotificationListenerService::class.java).flattenToString()
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ).orEmpty()
    return containsEnabledNotificationListener(enabledListeners, flatName)
}

internal fun containsEnabledNotificationListener(
    enabledListeners: String,
    flatName: String,
): Boolean {
    return enabledListeners
        .split(":")
        .any { it.trim().equals(flatName, ignoreCase = true) }
}
