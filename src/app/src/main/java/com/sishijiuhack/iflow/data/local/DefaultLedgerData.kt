package com.sishijiuhack.iflow.data.local

import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import com.sishijiuhack.iflow.domain.model.AccountType
import com.sishijiuhack.iflow.domain.model.TransactionType

object DefaultLedgerData {
    const val DefaultAccountId = 1L
    const val DefaultDirectionPattern = "收款|收入|到账|退款|转入|入账|存入|工资|付款|扣款|支出|消费|支付|转出"
    const val DefaultMerchantPattern =
        """(?:向|给|在)([^，,。]+?)(?:付款|支付|消费|转账)|(?:商户名称|交易商户|商户名|商户|交易对手|交易方|对手户名|收款户名|收款方|对方|付款方)[:：]\s*([^，,。]+)"""

    val categories = listOf(
        CategoryEntity(id = 1L, name = "餐饮", type = TransactionType.Expense, icon = "restaurant", sortOrder = 10, isDefault = true),
        CategoryEntity(id = 2L, name = "交通", type = TransactionType.Expense, icon = "directions_car", sortOrder = 20, isDefault = true),
        CategoryEntity(id = 3L, name = "购物", type = TransactionType.Expense, icon = "shopping_bag", sortOrder = 30, isDefault = true),
        CategoryEntity(id = 4L, name = "住房", type = TransactionType.Expense, icon = "home", sortOrder = 40, isDefault = true),
        CategoryEntity(id = 5L, name = "娱乐", type = TransactionType.Expense, icon = "movie", sortOrder = 50, isDefault = true),
        CategoryEntity(id = 6L, name = "医疗", type = TransactionType.Expense, icon = "medical_services", sortOrder = 60, isDefault = true),
        CategoryEntity(id = 7L, name = "转账", type = TransactionType.Expense, icon = "swap_horiz", sortOrder = 70, isDefault = true),
        CategoryEntity(id = 101L, name = "工资", type = TransactionType.Income, icon = "payments", sortOrder = 10, isDefault = true),
        CategoryEntity(id = 102L, name = "退款", type = TransactionType.Income, icon = "undo", sortOrder = 20, isDefault = true),
        CategoryEntity(id = 103L, name = "其他收入", type = TransactionType.Income, icon = "add_circle", sortOrder = 30, isDefault = true),
    )

    val accounts = listOf(
        AccountEntity(id = DefaultAccountId, name = "现金", type = AccountType.Cash, sortOrder = 10, isDefault = true),
        AccountEntity(id = 2L, name = "银行卡", type = AccountType.Bank, sortOrder = 20, isDefault = true),
        AccountEntity(id = 3L, name = "微信", type = AccountType.Wechat, sortOrder = 30, isDefault = true),
        AccountEntity(id = 4L, name = "支付宝", type = AccountType.Alipay, sortOrder = 40, isDefault = true),
        AccountEntity(id = 5L, name = "其他", type = AccountType.Other, sortOrder = 50, isDefault = true),
    )

    val appSetting = AppSettingEntity(
        id = AppSettingEntity.DefaultId,
        autoCaptureEnabled = true,
        autoConfirmEnabled = false,
        defaultAccountId = DefaultAccountId,
        lastExportedAt = null,
        createdAt = 0L,
        updatedAt = 0L,
    )

    val notificationRules = listOf(
        NotificationRuleEntity(
            id = 1L,
            packageName = "com.tencent.mm",
            appName = "微信",
            enabled = true,
            keywords = listOf("微信支付", "付款", "收款", "退款", "转账"),
            amountPattern = """(?:¥|￥|人民币|金额)\s*(\d+(?:\.\d{1,2})?)|(\d+(?:\.\d{1,2})?)\s*元""",
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
        NotificationRuleEntity(
            id = 2L,
            packageName = "com.eg.android.AlipayGphone",
            appName = "支付宝",
            enabled = true,
            keywords = listOf("支付宝", "支付", "付款", "收款", "退款", "转账"),
            amountPattern = """(?:¥|￥|人民币|金额)\s*(\d+(?:\.\d{1,2})?)|(\d+(?:\.\d{1,2})?)\s*元""",
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
        NotificationRuleEntity(
            id = 3L,
            packageName = "com.unionpay",
            appName = "银联",
            enabled = true,
            keywords = listOf("银联", "交易", "消费", "支出", "付款", "支付", "扣款", "转出", "转入", "入账", "存入", "收入", "到账"),
            amountPattern = """(?:¥|￥|人民币|金额)\s*(\d+(?:\.\d{1,2})?)|(\d+(?:\.\d{1,2})?)\s*元""",
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
        NotificationRuleEntity(
            id = 4L,
            packageName = "bank",
            appName = "银行",
            enabled = true,
            keywords = listOf("交易", "消费", "支出", "付款", "支付", "扣款", "转出", "转入", "入账", "存入", "工资", "收入", "到账", "退款"),
            amountPattern = """(?:¥|￥|人民币|金额)\s*(\d+(?:\.\d{1,2})?)|(\d+(?:\.\d{1,2})?)\s*元""",
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
    )
}
