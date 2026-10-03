package com.trea.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trea.app.TreAApplication
import com.trea.app.ui.home.HomeScreen
import com.trea.app.ui.home.HomeViewModel
import com.trea.app.ui.ledger.LedgerScreen
import com.trea.app.ui.ledger.LedgerViewModel
import com.trea.app.ui.music.MusicScreen
import com.trea.app.ui.music.MusicViewModel
import com.trea.app.ui.notes.NotesScreen
import com.trea.app.ui.notes.NotesViewModel
import com.trea.app.ui.settings.SettingsScreen
import com.trea.app.ui.settings.SettingsViewModel

@Composable
fun TreAMainNavigation(
    application: TreAApplication,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(text = screen.title)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(application.userPreferencesRepository)
                )
                HomeScreen(viewModel = homeViewModel)
            }

            composable(Screen.Music.route) {
                val musicViewModel: MusicViewModel = viewModel(
                    factory = MusicViewModel.provideFactory(
                        application.musicPlayerController,
                        application.musicScanner,
                        application.userPreferencesRepository
                    )
                )
                MusicScreen(
                    viewModel = musicViewModel,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Ledger.route) {
                val ledgerViewModel: LedgerViewModel = viewModel(
                    factory = LedgerViewModel.provideFactory(application.ledgerRepository)
                )
                LedgerScreen(viewModel = ledgerViewModel)
            }

            composable(Screen.Notes.route) {
                val notesViewModel: NotesViewModel = viewModel(
                    factory = NotesViewModel.provideFactory(application.noteRepository)
                )
                NotesScreen(viewModel = notesViewModel)
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(
                        application.userPreferencesRepository,
                        application.musicPlayerController,
                        application.musicScanner
                    )
                )
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
