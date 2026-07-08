package com.sishijiuhack.iflow.notification

import android.app.Notification
import android.os.Bundle

object NotificationPayloadExtractor {
    fun titleFrom(extras: Bundle): String {
        return (
            extras.getCharSequence(Notification.EXTRA_TITLE)
                ?: extras.getCharSequence(Notification.EXTRA_TITLE_BIG)
            )
            ?.toString()
            .orEmpty()
    }

    fun textFrom(extras: Bundle): String {
        return listOfNotNull(
            extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
            extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString(),
        )
            .plus(
                extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                    ?.map { it.toString() }
                    .orEmpty(),
            )
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = " ")
    }
}
