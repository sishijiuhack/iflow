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
    return enabledListeners.split(":").any { it.equals(flatName, ignoreCase = true) }
}
