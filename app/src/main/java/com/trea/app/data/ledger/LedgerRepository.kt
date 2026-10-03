package com.trea.app.data.ledger

import kotlinx.coroutines.flow.Flow

class LedgerRepository(private val ledgerDao: LedgerDao) {
    val allEntries: Flow<List<LedgerEntry>> = ledgerDao.getAllEntries()

    suspend fun addEntry(amount: Double, type: String, note: String): Long {
        val entry = LedgerEntry(
            amount = amount,
            type = type,
            note = note.trim(),
            timestamp = System.currentTimeMillis()
        )
        return ledgerDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: LedgerEntry) {
        ledgerDao.deleteEntry(entry)
    }

    suspend fun deleteById(id: Long) {
        ledgerDao.deleteById(id)
    }
}
