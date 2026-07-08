package com.sishijiuhack.iflow.notification

import com.sishijiuhack.iflow.core.model.MoneyParser
import com.sishijiuhack.iflow.domain.model.TransactionType
import java.security.MessageDigest

class PaymentNotificationParser {
    fun parse(input: PaymentNotificationInput): PaymentNotificationParseResult? {
        val combined = listOf(input.title, input.text).joinToString(" ")
        if (!looksLikePayment(input.packageName, combined)) return null

        val amountCents = extractAmountCents(combined)
            ?: return null

        val directionText = merchantLabelRegex.replace(combined, "")
        val hasStrongExpenseSignal = strongExpenseKeywords.any { directionText.contains(it) }
        val type = when {
            incomeKeywords.any { directionText.contains(it) } && !hasStrongExpenseSignal -> TransactionType.Income
            hasStrongExpenseSignal || expenseKeywords.any { directionText.contains(it) } -> TransactionType.Expense
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
            packageName.contains("unionpay", ignoreCase = true) -> "银联"
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

    private fun extractAmountCents(text: String): Long? {
        return amountRegex.findAll(text).firstNotNullOfOrNull { match ->
            if (match.isAuxiliaryAmount(text)) return@firstNotNullOfOrNull null
            match.groups
                .let { groups -> groups[1]?.value ?: groups[2]?.value }
                ?.replace(",", "")
                ?.replace("，", "")
                ?.let(MoneyParser::parseCents)
        }
    }

    private fun MatchResult.isAuxiliaryAmount(text: String): Boolean {
        val contextStart = (range.first - 8).coerceAtLeast(0)
        val leadingContext = text.substring(contextStart, range.first)
        return auxiliaryAmountLabels.any { leadingContext.contains(it) }
    }

    private companion object {
        private const val amountNumberPattern = """(?:[\d０-９]{1,3}(?:[,，][\d０-９]{3})+|[\d０-９]+)(?:[.．。][\d０-９]{1,2})?"""
        val amountRegex = Regex(
            """(?:¥|￥|人民币|RMB|CNY|金额)\s*[:：]?\s*($amountNumberPattern)|($amountNumberPattern)\s*(?:元|块)""",
            RegexOption.IGNORE_CASE,
        )
        val merchantRegexes = listOf(
            Regex("""(?:向|给|在)([^，,。]+?)(?:付款|支付|消费|转账)"""),
            Regex("""(?:$merchantLabelPattern)[:：]\s*(.+?)(?=$merchantBoundaryPattern|$)""", RegexOption.IGNORE_CASE),
        )
        val merchantLabelRegex = Regex(
            """(?:$merchantLabelPattern)[:：]\s*.+?(?=$merchantBoundaryPattern|$)""",
            RegexOption.IGNORE_CASE,
        )
        const val merchantLabelPattern = """商户名称|交易商户|商户名|商户|交易对手|交易方|对手户名|对方户名|收款户名|收款账户|收款人|收款方|对方|付款方|付款账户|付款户名|付款人"""
        const val merchantBoundaryPattern = """(?:[，,。；;]|\s+(?:余额|账户余额|可用余额|尾号|卡号|交易时间|时间|金额|人民币|RMB|CNY|¥|￥|优惠|立减|折扣|红包|抵扣|应付|原价|实付))"""
        val knownPackages = setOf(
            "com.tencent.mm",
            "com.eg.android.AlipayGphone",
            "com.unionpay",
        )
        val bankPackageHints = listOf(
            "icbc",
            "cmb",
            "ccb",
            "boc",
            "abchina",
            "psbc",
            "bankcomm",
            "spdb",
            "cib",
            "cebbank",
            "citic",
            "cmbc",
            "cgb",
            "cbhb",
            "bosc",
            "bankofbeijing",
            "bob",
            "hxb",
            "pingan",
        )
        val knownPackageHints = listOf("bank", "unionpay", "alipay") + bankPackageHints
        val paymentKeywords = listOf("支付", "付款", "扣款", "收款", "退款", "消费", "转账", "转入", "入账", "存入", "工资", "贷记", "借记", "交易", "支出", "收入", "到账")
        val incomeKeywords = listOf("收款", "收入", "到账", "退款", "转入", "入账", "存入", "工资", "贷记")
        val strongExpenseKeywords = listOf("付款", "扣款", "支出", "消费", "转出", "借记")
        val expenseKeywords = listOf("付款", "扣款", "支出", "消费", "支付", "转出", "借记")
        val auxiliaryAmountLabels = listOf("余额", "账户余额", "可用余额", "手续费", "服务费", "优惠", "立减", "折扣", "红包", "抵扣", "应付", "原价")
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
