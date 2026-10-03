package com.trea.app

import android.app.Application
import com.trea.app.data.ledger.LedgerDatabase
import com.trea.app.data.ledger.LedgerRepository
import com.trea.app.data.music.MusicPlayerController
import com.trea.app.data.music.MusicScanner
import com.trea.app.data.notes.NoteRepository
import com.trea.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TreAApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var ledgerRepository: LedgerRepository
        private set

    lateinit var noteRepository: NoteRepository
        private set

    lateinit var musicScanner: MusicScanner
        private set

    lateinit var musicPlayerController: MusicPlayerController
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(this)
        val ledgerDatabase = LedgerDatabase.getDatabase(this)
        ledgerRepository = LedgerRepository(ledgerDatabase.ledgerDao())
        noteRepository = NoteRepository(this)
        musicScanner = MusicScanner(this)
        musicPlayerController = MusicPlayerController(this, applicationScope)
    }

    override fun onTerminate() {
        musicPlayerController.release()
        super.onTerminate()
    }
}
