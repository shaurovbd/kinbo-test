package com.kinbo.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(Route.Home.route, "Home", Icons.Outlined.Home, Icons.Rounded.Home),
    AI(Route.AiAssistant.route, "AI", Icons.Outlined.AutoAwesome, Icons.Rounded.AutoAwesome),
    Budget(Route.Budget.route, "Budget", Icons.Outlined.AccountBalanceWallet, Icons.Rounded.AccountBalanceWallet),
    Analytics(Route.Analytics.route, "Stats", Icons.Outlined.BarChart, Icons.Rounded.BarChart),
    Settings(Route.Settings.route, "Settings", Icons.Outlined.Settings, Icons.Rounded.Settings),
}
