package com.kinbo.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.ui.platform.LocalContext
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.data.ListShare
import com.kinbo.app.ui.screens.*

@Composable
fun KinboNavHost(vm: KinboViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val context = LocalContext.current

    val showBottomBar = BottomTab.entries.any { it.route == current?.route } || current?.route == Route.Profile.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        val selected = current?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Route.Splash.route,
            modifier = Modifier.padding(inner),
        ) {
            composable(Route.Splash.route) {
                SplashScreen(onDone = {
                    nav.navigate(Route.Onboarding.route) { popUpTo(Route.Splash.route) { inclusive = true } }
                })
            }
            composable(Route.Onboarding.route) {
                OnboardingScreen(onDone = {
                    nav.navigate(Route.Login.route) { popUpTo(Route.Onboarding.route) { inclusive = true } }
                })
            }
            composable(Route.Login.route) {
                LoginScreen(
                    vm = vm,
                    onLogin = { nav.navigate(Route.Home.route) { popUpTo(Route.Login.route) { inclusive = true } } },
                    onSignup = { nav.navigate(Route.Signup.route) },
                )
            }
            composable(Route.Signup.route) {
                SignupScreen(
                    vm = vm,
                    onSignup = { nav.navigate(Route.Home.route) { popUpTo(Route.Signup.route) { inclusive = true } } },
                    onLogin = { nav.popBackStack() },
                )
            }
            composable(Route.Home.route) {
                HomeScreen(
                    vm = vm,
                    onOpenList = { nav.navigate(Route.ShoppingList.create(it)) },
                    onCreateList = { nav.navigate(Route.CreateList.route) },
                    onNotifications = { nav.navigate(Route.Notifications.route) },
                    onProfile = { nav.navigate(Route.Profile.route) },
                    onScan = { listId -> nav.navigate(Route.Scanner.create(listId)) },
                    onShare = { list -> ListShare.share(context, list) },
                )
            }
            composable(Route.CreateList.route) {
                CreateListScreen(
                    vm = vm,
                    onCreated = { id -> nav.navigate(Route.ShoppingList.create(id)) { popUpTo(Route.CreateList.route) { inclusive = true } } },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Route.ShoppingList.route, arguments = listOf(navArgument("listId") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("listId").orEmpty()
                ShoppingListScreen(
                    vm = vm, listId = id, onBack = { nav.popBackStack() },
                    onAddItem = { nav.navigate(Route.AddItem.create(id)) },
                    onAI = { nav.navigate(Route.AiAssistant.route) },
                    onScan = { nav.navigate(Route.Scanner.create(id)) },
                    onShare = { list -> ListShare.share(context, list) },
                    onCollab = { nav.navigate(Route.Collaborators.create(id)) },
                )
            }
            composable(Route.Scanner.route, arguments = listOf(navArgument("listId") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("listId").orEmpty()
                ScannerScreen(vm = vm, listId = id, onBack = { nav.popBackStack() })
            }
            composable(Route.Collaborators.route, arguments = listOf(navArgument("listId") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("listId").orEmpty()
                CollaboratorScreen(vm = vm, listId = id, onBack = { nav.popBackStack() })
            }
            composable(Route.AddItem.route, arguments = listOf(navArgument("listId") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("listId").orEmpty()
                AddItemScreen(vm = vm, listId = id, onDone = { nav.popBackStack() }, onBack = { nav.popBackStack() })
            }
            composable(Route.AiAssistant.route) { AiAssistantScreen(vm = vm, onBack = { nav.popBackStack() }) }
            composable(Route.Budget.route) { BudgetScreen(vm = vm, onBack = { nav.popBackStack() }) }
            composable(Route.Analytics.route) { AnalyticsScreen(vm = vm, onBack = { nav.popBackStack() }) }
            composable(Route.Notifications.route) { NotificationsScreen(vm = vm, onBack = { nav.popBackStack() }) }
            composable(Route.Settings.route) { SettingsScreen(vm = vm, onProfile = { nav.navigate(Route.Profile.route) }) }
            composable(Route.Profile.route) { ProfileScreen(vm = vm, onBack = { nav.popBackStack() }) }
        }
    }
}
