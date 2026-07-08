package com.sishijiuhack.iflow.notification

import android.app.Notification
import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NotificationPayloadExtractorTest {
    @Test
    fun textFrom_includesMultiLineNotificationText() {
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE, "交易提醒")
            putCharSequence(Notification.EXTRA_TEXT, "支出人民币16.20元")
            putCharSequence(Notification.EXTRA_BIG_TEXT, "商户：地铁")
            putCharSequenceArray(
                Notification.EXTRA_TEXT_LINES,
                arrayOf("尾号1234", "余额人民币1,000.00元"),
            )
        }

        assertEquals("交易提醒", NotificationPayloadExtractor.titleFrom(extras))
        assertEquals(
            "支出人民币16.20元 商户：地铁 尾号1234 余额人民币1,000.00元",
            NotificationPayloadExtractor.textFrom(extras),
        )
    }
}
