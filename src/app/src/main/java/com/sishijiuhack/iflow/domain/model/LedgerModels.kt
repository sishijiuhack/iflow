package com.sishijiuhack.iflow.domain.model

enum class TransactionType {
    Expense,
    Income,
}

enum class AccountType {
    Cash,
    Bank,
    Wechat,
    Alipay,
    Other,
}

enum class TransactionSource {
    Manual,
    Notification,
}

enum class TransactionStatus {
    Pending,
    Confirmed,
    Deleted,
}
