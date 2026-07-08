package com.sishijiuhack.iflow.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.sishijiuhack.iflow.core.android.appContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PaymentNotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val parser = PaymentNotificationParser()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = listOf(
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty(),
        ).filter { it.isNotBlank() }.joinToString(" ")

        val parsed = parser.parse(
            PaymentNotificationInput(
                packageName = sbn.packageName,
                title = title,
                text = text,
                postedAt = sbn.postTime,
            ),
        ) ?: return

        serviceScope.launch {
            appContainer().ledgerRepository.savePendingNotificationTransaction(parsed)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
