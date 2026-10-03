package com.trea.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.trea.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val timeString: String = "",
    val secondsString: String = "",
    val dateString: String = "",
    val greeting: String = "",
    val quote: String = ""
)

class HomeViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val quotes = listOf(
        "生活不需要被解释，只需要被经历。",
        "慢品人间烟火气，闲饮人间岁月长。",
        "保持热爱，奔赴山海。",
        "日出有盼，日落有念，平淡生活也有暖意。",
        "路虽远，行则将至；事虽难，做则必成。",
        "向阳而生，逐光而行。",
        "心怀浪漫宇宙，也珍惜人间日常。",
        "知足且上进，温柔且坚定。",
        "每一天都是独一无二的礼物。",
        "在自己的节奏里，过好属于自己的生活。",
        "云淡风轻的日子，最值得细细品味。",
        "凡是过往，皆为序章；所有未来，皆可期待。",
        "心若向阳，无谓悲伤。",
        "行而不辍，未来可期。",
        "保持简单的初心，享受纯粹的宁静。"
    )

    private val _currentQuote = MutableStateFlow(quotes.random())
    private val _currentTime = MutableStateFlow(LocalDateTime.now())

    val uiState: StateFlow<HomeUiState> = combine(
        _currentTime,
        preferencesRepository.nicknameFlow,
        _currentQuote
    ) { time, nickname, quote ->
        val hour = time.hour
        val greetingPrefix = when (hour) {
            in 5..11 -> "早上好"
            in 12..17 -> "下午好"
            in 18..22 -> "晚上好"
            else -> "夜深了"
        }
        val greeting = "$greetingPrefix，$nickname"

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val secondsFormatter = DateTimeFormatter.ofPattern("ss")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINESE)

        HomeUiState(
            timeString = time.format(timeFormatter),
            secondsString = time.format(secondsFormatter),
            dateString = time.format(dateFormatter),
            greeting = greeting,
            quote = quote
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        startClock()
    }

    private fun startClock() {
        viewModelScope.launch {
            while (isActive) {
                _currentTime.value = LocalDateTime.now()
                delay(1000)
            }
        }
    }

    fun nextQuote() {
        _currentQuote.update { current ->
            val pool = quotes.filter { it != current }
            if (pool.isNotEmpty()) pool.random() else current
        }
    }

    companion object {
        fun provideFactory(
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(preferencesRepository) as T
            }
        }
    }
}
