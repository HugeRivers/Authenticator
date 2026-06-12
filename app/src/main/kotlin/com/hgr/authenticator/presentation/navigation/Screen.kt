package com.hgr.authenticator.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddAccount : Screen("add_account")
    data object Settings : Screen("settings")
    data object Export : Screen("export")
}
