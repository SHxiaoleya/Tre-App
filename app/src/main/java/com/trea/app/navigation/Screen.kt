package com.trea.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    data object Home : Screen(
        route = "home",
        title = "主页",
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    )

    data object Music : Screen(
        route = "music",
        title = "音乐",
        unselectedIcon = Icons.Outlined.MusicNote,
        selectedIcon = Icons.Filled.MusicNote
    )

    data object Ledger : Screen(
        route = "ledger",
        title = "账本",
        unselectedIcon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet
    )

    data object Notes : Screen(
        route = "notes",
        title = "笔记",
        unselectedIcon = Icons.Outlined.Description,
        selectedIcon = Icons.Filled.Description
    )

    data object Settings : Screen(
        route = "settings",
        title = "设置",
        unselectedIcon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )

    companion object {
        val bottomNavItems = listOf(Home, Music, Ledger, Notes, Settings)
    }
}
