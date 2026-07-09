package com.sishijiuhack.iflow.data.local

import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.data.local.entity.NotificationRuleEntity
import com.sishijiuhack.iflow.domain.model.AccountType
import com.sishijiuhack.iflow.domain.model.TransactionType

object DefaultLedgerData {
    const val DefaultAccountId = 1L
    const val DefaultAmountPattern =
        """(?:¥|￥|人民币|(?i:RMB|CNY)|金额)[\s\u00A0\u202F]*[:：]?[\s\u00A0\u202F]*((?:[\d０-９]{1,3}(?:[,，][\d０-９]{3})+|[\d０-９]+)(?:[.．。][\d０-９]{1,2})?)|((?:[\d０-９]{1,3}(?:[,，][\d０-９]{3})+|[\d０-９]+)(?:[.．。][\d０-９]{1,2})?)[\s\u00A0\u202F]*(?:元|块)"""
    const val DefaultDirectionPattern = "收款|收入|到账|退款|转入|入账|存入|工资|贷记|付款|扣款|支出|消费|支付|转出|借记"
    const val DefaultMerchantPattern =
        """(?:向|给|在)([^，,。]+?)(?:付款|支付|消费|转账)|(?:商户名称|交易商户|商户名|商户|交易对手|交易方|对手户名|对方户名|收款户名|收款账户|收款人|收款方|对方|付款方|付款账户|付款户名|付款人)[:：][\s\u00A0\u202F]*([^，,。]+)"""

    val categories = listOf(
        CategoryEntity(id = 1L, name = "餐饮", type = TransactionType.Expense, icon = "restaurant", sortOrder = 10, isDefault = true),
        CategoryEntity(id = 2L, name = "交通", type = TransactionType.Expense, icon = "directions_car", sortOrder = 20, isDefault = true),
        CategoryEntity(id = 3L, name = "购物", type = TransactionType.Expense, icon = "shopping_bag", sortOrder = 30, isDefault = true),
        CategoryEntity(id = 4L, name = "住房", type = TransactionType.Expense, icon = "home", sortOrder = 40, isDefault = true),
        CategoryEntity(id = 5L, name = "娱乐", type = TransactionType.Expense, icon = "movie", sortOrder = 50, isDefault = true),
        CategoryEntity(id = 6L, name = "医疗", type = TransactionType.Expense, icon = "medical_services", sortOrder = 60, isDefault = true),
        CategoryEntity(id = 7L, name = "转账", type = TransactionType.Expense, icon = "swap_horiz", sortOrder = 70, isDefault = true),
        CategoryEntity(id = 8L, name = "通讯", type = TransactionType.Expense, icon = "phone", sortOrder = 80, isDefault = true),
        CategoryEntity(id = 9L, name = "教育", type = TransactionType.Expense, icon = "school", sortOrder = 90, isDefault = true),
        CategoryEntity(id = 10L, name = "服饰", type = TransactionType.Expense, icon = "checkroom", sortOrder = 100, isDefault = true),
        CategoryEntity(id = 11L, name = "美妆", type = TransactionType.Expense, icon = "spa", sortOrder = 110, isDefault = true),
        CategoryEntity(id = 12L, name = "社交", type = TransactionType.Expense, icon = "groups", sortOrder = 120, isDefault = true),
        CategoryEntity(id = 13L, name = "旅行", type = TransactionType.Expense, icon = "flight", sortOrder = 130, isDefault = true),
        CategoryEntity(id = 14L, name = "宠物", type = TransactionType.Expense, icon = "pets", sortOrder = 140, isDefault = true),
        CategoryEntity(id = 15L, name = "育儿", type = TransactionType.Expense, icon = "child_care", sortOrder = 150, isDefault = true),
        CategoryEntity(id = 16L, name = "家居", type = TransactionType.Expense, icon = "chair", sortOrder = 160, isDefault = true),
        CategoryEntity(id = 17L, name = "数码", type = TransactionType.Expense, icon = "devices", sortOrder = 170, isDefault = true),
        CategoryEntity(id = 18L, name = "运动", type = TransactionType.Expense, icon = "fitness_center", sortOrder = 180, isDefault = true),
        CategoryEntity(id = 19L, name = "保险", type = TransactionType.Expense, icon = "health_and_safety", sortOrder = 190, isDefault = true),
        CategoryEntity(id = 20L, name = "税费", type = TransactionType.Expense, icon = "receipt_long", sortOrder = 200, isDefault = true),
        CategoryEntity(id = 21L, name = "其他", type = TransactionType.Expense, icon = "more_horiz", sortOrder = 210, isDefault = true),
        CategoryEntity(id = 101L, name = "工资", type = TransactionType.Income, icon = "payments", sortOrder = 10, isDefault = true),
        CategoryEntity(id = 102L, name = "奖金", type = TransactionType.Income, icon = "redeem", sortOrder = 20, isDefault = true),
        CategoryEntity(id = 103L, name = "兼职", type = TransactionType.Income, icon = "work", sortOrder = 30, isDefault = true),
        CategoryEntity(id = 104L, name = "投资", type = TransactionType.Income, icon = "trending_up", sortOrder = 40, isDefault = true),
        CategoryEntity(id = 105L, name = "理财", type = TransactionType.Income, icon = "savings", sortOrder = 50, isDefault = true),
        CategoryEntity(id = 106L, name = "红包", type = TransactionType.Income, icon = "card_giftcard", sortOrder = 60, isDefault = true),
        CategoryEntity(id = 107L, name = "退款", type = TransactionType.Income, icon = "undo", sortOrder = 70, isDefault = true),
        CategoryEntity(id = 108L, name = "其他收入", type = TransactionType.Income, icon = "add_circle", sortOrder = 80, isDefault = true),
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
            amountPattern = DefaultAmountPattern,
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
        NotificationRuleEntity(
            id = 2L,
            packageName = "com.eg.android.AlipayGphone",
            appName = "支付宝",
            enabled = true,
            keywords = listOf("支付宝", "支付", "付款", "收款", "退款", "转账"),
            amountPattern = DefaultAmountPattern,
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
        NotificationRuleEntity(
            id = 3L,
            packageName = "com.unionpay",
            appName = "银联",
            enabled = true,
            keywords = listOf("银联", "交易", "消费", "支出", "付款", "支付", "扣款", "转出", "借记", "转入", "入账", "存入", "贷记", "收入", "到账"),
            amountPattern = DefaultAmountPattern,
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
        NotificationRuleEntity(
            id = 4L,
            packageName = "bank",
            appName = "银行",
            enabled = true,
            keywords = listOf("交易", "消费", "支出", "付款", "支付", "扣款", "转出", "借记", "转入", "入账", "存入", "工资", "贷记", "收入", "到账", "退款"),
            amountPattern = DefaultAmountPattern,
            directionPattern = DefaultDirectionPattern,
            merchantPattern = DefaultMerchantPattern,
        ),
    )
}
