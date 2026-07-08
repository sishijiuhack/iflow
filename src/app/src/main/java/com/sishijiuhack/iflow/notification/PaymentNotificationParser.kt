package com.sishijiuhack.iflow.notification

import com.sishijiuhack.iflow.core.model.MoneyParser
import com.sishijiuhack.iflow.domain.model.TransactionType
import java.security.MessageDigest

class PaymentNotificationParser {
    fun parse(input: PaymentNotificationInput): PaymentNotificationParseResult? {
        val combined = listOf(input.title, input.text).joinToString(" ")
        if (!looksLikePayment(input.packageName, combined)) return null

        val amountCents = amountRegex.find(combined)
            ?.groups
            ?.let { groups -> groups[1]?.value ?: groups[2]?.value }
            ?.let(MoneyParser::parseCents)
            ?: return null

        val type = when {
            incomeKeywords.any { combined.contains(it) } -> TransactionType.Income
            expenseKeywords.any { combined.contains(it) } -> TransactionType.Expense
            else -> TransactionType.Expense
        }

        return PaymentNotificationParseResult(
            amountCents = amountCents,
            type = type,
            merchant = extractMerchant(combined),
            sourceApp = sourceName(input.packageName),
            fingerprint = NotificationFingerprint.create(
                packageName = input.packageName,
                title = input.title,
                text = input.text,
                postedAt = input.postedAt,
                amountCents = amountCents,
            ),
            postedAt = input.postedAt,
            rawTitle = input.title,
            rawText = input.text,
            packageName = input.packageName,
        )
    }

    private fun looksLikePayment(packageName: String, text: String): Boolean {
        val knownPackage = packageName in knownPackages ||
            knownPackageHints.any { packageName.contains(it, ignoreCase = true) }
        val hasKeyword = paymentKeywords.any { text.contains(it) }
        return knownPackage && hasKeyword
    }

    private fun sourceName(packageName: String): String {
        return when {
            packageName == "com.tencent.mm" -> "微信"
            packageName == "com.eg.android.AlipayGphone" -> "支付宝"
            packageName.contains("unionpay") -> "银联"
            packageName.contains("bank", ignoreCase = true) -> "银行"
            bankPackageHints.any { packageName.contains(it, ignoreCase = true) } -> "银行"
            packageName.contains("alipay", ignoreCase = true) -> "支付宝"
            else -> packageName
        }
    }

    private fun extractMerchant(text: String): String? {
        val merchant = merchantRegexes.firstNotNullOfOrNull { regex ->
            regex.find(text)?.groups?.get(1)?.value
        }
        return merchant?.trim(' ', '，', ',', '。')?.takeIf { it.isNotBlank() }
    }

    private companion object {
        val amountRegex = Regex("""(?:¥|￥|人民币|金额)\s*(\d+(?:\.\d{1,2})?)|(\d+(?:\.\d{1,2})?)\s*元""")
        val merchantRegexes = listOf(
            Regex("""(?:向|给|在)([^，,。]+?)(?:付款|支付|消费|转账)"""),
            Regex("""(?:商户|收款方|对方|付款方)[:：]\s*([^，,。]+)"""),
        )
        val knownPackages = setOf(
            "com.tencent.mm",
            "com.eg.android.AlipayGphone",
            "com.unionpay",
        )
        val bankPackageHints = listOf("icbc", "cmb", "ccb", "boc", "abchina", "psbc", "bankcomm")
        val knownPackageHints = listOf("bank", "unionpay", "alipay") + bankPackageHints
        val paymentKeywords = listOf("支付", "付款", "收款", "退款", "消费", "转账", "交易", "支出", "收入", "到账")
        val incomeKeywords = listOf("收款", "收入", "到账", "退款", "转入")
        val expenseKeywords = listOf("付款", "支出", "消费", "支付", "转出")
    }
}

data class PaymentNotificationInput(
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
)

data class PaymentNotificationParseResult(
    val amountCents: Long,
    val type: TransactionType,
    val merchant: String?,
    val sourceApp: String,
    val fingerprint: String,
    val postedAt: Long,
    val rawTitle: String,
    val rawText: String,
    val packageName: String,
)

object NotificationFingerprint {
    fun create(
        packageName: String,
        title: String,
        text: String,
        postedAt: Long,
        amountCents: Long,
    ): String {
        val bucketedTime = postedAt / 60_000L
        val raw = listOf(packageName, title, text, bucketedTime.toString(), amountCents.toString())
            .joinToString(separator = "|")
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
