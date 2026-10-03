package com.trea.app.data.ledger

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_entries")
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isIncome: Boolean get() = type == TYPE_INCOME

    companion object {
        const val TYPE_INCOME = "INCOME"
        const val TYPE_EXPENSE = "EXPENSE"
    }
}
