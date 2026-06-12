package com.hgr.authenticator.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hgr.authenticator.presentation.addaccount.AddAccountScreen
import com.hgr.authenticator.presentation.common.Transitions
import com.hgr.authenticator.presentation.export.ExportScreen
import com.hgr.authenticator.presentation.home.HomeScreen
import com.hgr.authenticator.presentation.settings.SettingsScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { Transitions.slideInFromRight },
            exitTransition = { Transitions.slideOutToLeft },
            popEnterTransition = { Transitions.slideInFromLeft },
            popExitTransition = { Transitions.slideOutToRight }
        ) {
            HomeScreen(
                onNavigateToAddAccount = { navController.navigate(Screen.AddAccount.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.AddAccount.route,
            enterTransition = { Transitions.slideInFromRight },
            exitTransition = { Transitions.slideOutToLeft },
            popEnterTransition = { Transitions.slideInFromLeft },
            popExitTransition = { Transitions.slideOutToRight }
        ) {
            AddAccountScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = { Transitions.slideInFromRight },
            exitTransition = { Transitions.slideOutToLeft },
            popEnterTransition = { Transitions.slideInFromLeft },
            popExitTransition = { Transitions.slideOutToRight }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExport = { navController.navigate(Screen.Export.route) }
            )
        }

        composable(
            route = Screen.Export.route,
            enterTransition = { Transitions.slideInFromRight },
            exitTransition = { Transitions.slideOutToLeft },
            popEnterTransition = { Transitions.slideInFromLeft },
            popExitTransition = { Transitions.slideOutToRight }
        ) {
            ExportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
