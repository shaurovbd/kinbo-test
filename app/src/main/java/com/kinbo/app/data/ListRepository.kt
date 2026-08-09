package com.kinbo.app.data

import com.kinbo.app.model.Budget
import com.kinbo.app.model.KinboNotification
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.model.ShoppingList
import com.kinbo.app.model.User
import com.kinbo.app.model.WeeklySpending
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository contract for list data.
 *
 * Two implementations exist:
 *  - [KinboRepository]   — local in-memory store (offline-first, always works)
 *  - [FirestoreSyncRepository] — real-time cloud sync via Firebase Firestore
 *
 * [SyncManager] selects which one is active based on whether Firebase is configured.
 * The ViewModel talks only to this interface, so swapping backends requires no UI changes.
 */
interface ListRepository {
    val user: StateFlow<User>
    val lists: StateFlow<List<ShoppingList>>
    val budget: StateFlow<Budget>
    val expenses: StateFlow<List<com.kinbo.app.model.ExpenseEntry>>
    val notifications: StateFlow<List<KinboNotification>>
    val weeklySpending: StateFlow<List<WeeklySpending>>
    val favorites: StateFlow<List<String>>
    val premium: StateFlow<Boolean>

    /** True when changes sync to the cloud and across devices in real time. */
    val isSynced: Boolean

    fun categoryShare(): List<com.kinbo.app.model.CategoryShare>

    // List operations
    fun toggleFavorite(id: String)
    fun archiveList(id: String)
    fun deleteList(id: String)
    fun duplicateList(id: String)
    fun renameList(id: String, newName: String)
    fun addList(name: String, emoji: String): ShoppingList
    fun getList(id: String): ShoppingList?

    // Item operations
    fun addItem(listId: String, item: ShoppingItem)
    fun updateItem(listId: String, item: ShoppingItem)
    fun removeItem(listId: String, itemId: String)
    fun togglePurchased(listId: String, itemId: String)
    fun sortItemsByCategory(listId: String)

    // Budget
    fun setBudget(limit: Double)

    // Notifications
    fun markAllNotificationsRead()

    // Auth
    fun login(email: String)
    fun signup(name: String, email: String)
    fun setPremium(v: Boolean)

    // Collaboration
    fun generateShareCode(listId: String): String
    fun joinListByShareCode(shareCode: String): ShoppingList?
    fun addCollaborator(listId: String, collaborator: com.kinbo.app.model.Collaborator)
    fun removeCollaborator(listId: String, userId: String)
    fun updateCollaboratorRole(listId: String, userId: String, role: com.kinbo.app.model.ListRole)
}
