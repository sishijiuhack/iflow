package com.sishijiuhack.iflow.data.local

import androidx.room.TypeConverter
import com.sishijiuhack.iflow.domain.model.AccountType
import com.sishijiuhack.iflow.domain.model.TransactionSource
import com.sishijiuhack.iflow.domain.model.TransactionStatus
import com.sishijiuhack.iflow.domain.model.TransactionType

class RoomConverters {
    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun transactionSourceToString(value: TransactionSource): String = value.name

    @TypeConverter
    fun stringToTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    @TypeConverter
    fun transactionStatusToString(value: TransactionStatus): String = value.name

    @TypeConverter
    fun stringToTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun accountTypeToString(value: AccountType): String = value.name

    @TypeConverter
    fun stringToAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun stringListToString(value: List<String>): String = value.joinToString(separator = "\n")

    @TypeConverter
    fun stringToStringList(value: String): List<String> {
        return value.split("\n").filter { it.isNotBlank() }
    }
}
