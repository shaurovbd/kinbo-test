package com.kinbo.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.kinbo.app.R

enum class BottomTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(Route.Home.route, R.string.nav_home, Icons.Outlined.Home, Icons.Rounded.Home),
    AI(Route.AiAssistant.route, R.string.nav_ai, Icons.Outlined.AutoAwesome, Icons.Rounded.AutoAwesome),
    Budget(Route.Budget.route, R.string.nav_budget, Icons.Outlined.AccountBalanceWallet, Icons.Rounded.AccountBalanceWallet),
    Analytics(Route.Analytics.route, R.string.nav_stats, Icons.Outlined.BarChart, Icons.Rounded.BarChart),
    Settings(Route.Settings.route, R.string.nav_settings, Icons.Outlined.Settings, Icons.Rounded.Settings),
}
