package com.sishijiuhack.iflow.data.local

import com.sishijiuhack.iflow.data.local.entity.AccountEntity
import com.sishijiuhack.iflow.data.local.entity.AppSettingEntity
import com.sishijiuhack.iflow.data.local.entity.CategoryEntity
import com.sishijiuhack.iflow.domain.model.AccountType
import com.sishijiuhack.iflow.domain.model.TransactionType

object DefaultLedgerData {
    const val DefaultAccountId = 1L

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
}
