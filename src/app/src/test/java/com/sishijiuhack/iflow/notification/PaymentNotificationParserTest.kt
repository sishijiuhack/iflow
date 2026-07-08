package com.sishijiuhack.iflow.notification

import com.sishijiuhack.iflow.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentNotificationParserTest {
    private val parser = PaymentNotificationParser()

    @Test
    fun parse_wechatPayment_extractsExpenseAmountAndMerchant() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.tencent.mm",
                title = "微信支付",
                text = "向便利店付款12.50元",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(1250L, result?.amountCents)
        assertEquals("便利店", result?.merchant)
        assertEquals("微信", result?.sourceApp)
    }

    @Test
    fun parse_refundNotification_extractsIncome() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.eg.android.AlipayGphone",
                title = "支付宝",
                text = "退款到账 8.00元 商户：咖啡店",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Income, result?.type)
        assertEquals(800L, result?.amountCents)
    }

    @Test
    fun parse_irrelevantNotification_returnsNull() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.weather",
                title = "天气",
                text = "今天多云",
                postedAt = 100_000L,
            ),
        )

        assertNull(result)
    }

    @Test
    fun fingerprint_sameNotificationInSameMinute_isStable() {
        val first = NotificationFingerprint.create("pkg", "title", "text", 120_000L, 100L)
        val second = NotificationFingerprint.create("pkg", "title", "text", 150_000L, 100L)
        val differentAmount = NotificationFingerprint.create("pkg", "title", "text", 150_000L, 200L)

        assertEquals(first, second)
        assertNotEquals(first, differentAmount)
    }
}
