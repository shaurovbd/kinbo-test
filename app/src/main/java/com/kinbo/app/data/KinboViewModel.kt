package com.kinbo.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.model.ShoppingList
import com.kinbo.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class KinboViewModel : ViewModel() {
    private val repo = KinboRepository()

    val user get() = repo.user
    val lists get() = repo.lists
    val budget get() = repo.budget
    val expenses get() = repo.expenses
    val notifications get() = repo.notifications
    val weeklySpending get() = repo.weeklySpending
    val favorites get() = repo.favorites
    val premium get() = repo.premium

    private val _themeMode = MutableStateFlow(ThemeMode.System)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) { _themeMode.value = mode }

    fun activeLists(): List<ShoppingList> = lists.value.filter { it.active }
    fun recentLists(): List<ShoppingList> = lists.value.sortedByDescending { it.updatedAt }
    fun favoriteLists(): List<ShoppingList> = lists.value.filter { it.favorite }

    fun categoryShare() = repo.categoryShare()

    fun toggleFavorite(id: String) = repo.toggleFavorite(id)
    fun archiveList(id: String) = repo.archiveList(id)
    fun deleteList(id: String) = repo.deleteList(id)
    fun duplicateList(id: String) = repo.duplicateList(id)
    fun renameList(id: String, name: String) = repo.renameList(id, name)
    fun addList(name: String, emoji: String): ShoppingList = repo.addList(name, emoji)
    fun getList(id: String): ShoppingList? = repo.getList(id)

    fun addItem(listId: String, item: ShoppingItem) = repo.addItem(listId, item)
    fun updateItem(listId: String, item: ShoppingItem) = repo.updateItem(listId, item)
    fun removeItem(listId: String, itemId: String) = repo.removeItem(listId, itemId)
    fun togglePurchased(listId: String, itemId: String) = repo.togglePurchased(listId, itemId)
    fun sortItemsByCategory(listId: String) = repo.sortItemsByCategory(listId)

    fun setBudget(limit: Double) = repo.setBudget(limit)
    fun markAllNotificationsRead() = repo.markAllNotificationsRead()

    fun login(email: String) = repo.login(email)
    fun signup(name: String, email: String) = repo.signup(name, email)
    fun setPremium(v: Boolean) = repo.setPremium(v)
}
