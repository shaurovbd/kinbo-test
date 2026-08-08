package com.kinbo.app.ui.navigation

sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Login : Route("login")
    data object Signup : Route("signup")
    data object Home : Route("home")
    data object ShoppingList : Route("list/{listId}") {
        fun create(id: String) = "list/$id"
    }
    data object AddItem : Route("add_item/{listId}") {
        fun create(id: String) = "add_item/$id"
    }
    data object AiAssistant : Route("ai")
    data object Budget : Route("budget")
    data object Analytics : Route("analytics")
    data object Notifications : Route("notifications")
    data object Settings : Route("settings")
    data object Profile : Route("profile")
    data object CreateList : Route("create_list")
}
