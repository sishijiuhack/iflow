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
    fun parse_alipayPayment_extractsExpenseMerchantAndSource() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.eg.android.AlipayGphone",
                title = "支付宝",
                text = "你已向星巴克支付￥32.80",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(3280L, result?.amountCents)
        assertEquals("星巴克", result?.merchant)
        assertEquals("支付宝", result?.sourceApp)
    }

    @Test
    fun parse_alipayPackageVariant_usesReadableSourceName() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.Alipay.mobile",
                title = "\u652f\u4ed8\u5b9d",
                text = "\u4f60\u5df2\u5411\u661f\u5df4\u514b\u652f\u4ed8\uffe532.80",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals("\u652f\u4ed8\u5b9d", result?.sourceApp)
    }

    @Test
    fun parse_bankExpenseWithOnlySpendingKeyword_extractsExpense() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.example.bank",
                title = "交易提醒",
                text = "尾号1234支出人民币16.20元，商户：地铁",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(1620L, result?.amountCents)
        assertEquals("地铁", result?.merchant)
        assertEquals("银行", result?.sourceApp)
    }

    @Test
    fun parse_commonBankPackageVariant_extractsBankSource() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.icbc.mobilebank",
                title = "交易提醒",
                text = "尾号1234支出人民币16.20元，商户：地铁",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(1620L, result?.amountCents)
        assertEquals("地铁", result?.merchant)
        assertEquals("银行", result?.sourceApp)
    }

    @Test
    fun parse_bankMerchantNameLabel_extractsMerchant() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.example.bank",
                title = "交易提醒",
                text = "尾号1234支出人民币16.20元，商户名称：地铁",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(1620L, result?.amountCents)
        assertEquals("地铁", result?.merchant)
    }

    @Test
    fun parse_unionPayExpense_extractsExpenseMerchantAndSource() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.UnionPay.wallet",
                title = "银联交易提醒",
                text = "消费￥45.60，商户：超市",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(4560L, result?.amountCents)
        assertEquals("超市", result?.merchant)
        assertEquals("银联", result?.sourceApp)
    }

    @Test
    fun parse_amountWithThousandsSeparator_extractsFullAmount() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.example.bank",
                title = "交易提醒",
                text = "消费￥1,234.56，商户：家电城",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(123456L, result?.amountCents)
        assertEquals("家电城", result?.merchant)
    }

    @Test
    fun parse_amountWithFullWidthThousandsSeparator_extractsFullAmount() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.example.bank",
                title = "交易提醒",
                text = "支出1，234.56元，商户：家电城",
                postedAt = 100_000L,
            ),
        )

        assertNotNull(result)
        assertEquals(TransactionType.Expense, result?.type)
        assertEquals(123456L, result?.amountCents)
        assertEquals("家电城", result?.merchant)
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
    fun parse_knownPackageAmountWithoutPaymentKeyword_returnsNull() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.tencent.mm",
                title = "Security notice",
                text = "Login risk score 12.00鍏?",
                postedAt = 100_000L,
            ),
        )

        assertNull(result)
    }

    @Test
    fun parse_zeroAmountPayment_returnsNull() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.tencent.mm",
                title = "微信支付",
                text = "向便利店付款0.00元",
                postedAt = 100_000L,
            ),
        )

        assertNull(result)
    }

    @Test
    fun parse_unknownPackagePaymentKeyword_returnsNull() {
        val result = parser.parse(
            PaymentNotificationInput(
                packageName = "com.random.app",
                title = "閾惰仈浜ゆ槗鎻愰啋",
                text = "娑堣垂锟?5.60锛屽晢鎴凤細瓒呭競",
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
