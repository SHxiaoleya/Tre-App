package com.trea.app.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trea.app.data.ledger.LedgerEntry
import com.trea.app.data.ledger.LedgerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LedgerSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0
)

data class LedgerUiState(
    val entries: List<LedgerEntry> = emptyList(),
    val summary: LedgerSummary = LedgerSummary()
)

class LedgerViewModel(
    private val repository: LedgerRepository
) : ViewModel() {

    val uiState: StateFlow<LedgerUiState> = repository.allEntries.map { list ->
        var income = 0.0
        var expense = 0.0
        for (item in list) {
            if (item.isIncome) {
                income += item.amount
            } else {
                expense += item.amount
            }
        }
        val balance = income - expense
        LedgerUiState(
            entries = list,
            summary = LedgerSummary(
                totalIncome = income,
                totalExpense = expense,
                balance = balance
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LedgerUiState()
    )

    fun addEntry(amount: Double, isIncome: Boolean, note: String) {
        if (amount <= 0) return
        val type = if (isIncome) LedgerEntry.TYPE_INCOME else LedgerEntry.TYPE_EXPENSE
        viewModelScope.launch {
            repository.addEntry(amount, type, note)
        }
    }

    fun deleteEntry(entry: LedgerEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    companion object {
        fun provideFactory(
            repository: LedgerRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LedgerViewModel(repository) as T
            }
        }
    }
}
