package com.kinbo.app.data

import com.kinbo.app.model.Budget
import com.kinbo.app.model.CategoryShare
import com.kinbo.app.model.Collaborator
import com.kinbo.app.model.ExpenseEntry
import com.kinbo.app.model.ItemCategory
import com.kinbo.app.model.KinboNotification
import com.kinbo.app.model.ListRole
import com.kinbo.app.model.Priority
import com.kinbo.app.model.ShoppingItem
import com.kinbo.app.model.ShoppingList
import com.kinbo.app.model.User
import com.kinbo.app.model.WeeklySpending
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * In-memory offline-first repository. Always works without a backend.
 * Implements [ListRepository] so the UI is backend-agnostic.
 * In production this would be backed by Firebase Firestore per the PRD — see [FirestoreSyncRepository].
 */
class KinboRepository : ListRepository {

    private val _user = MutableStateFlow(User("Aisha Khan", "aisha@kinbo.app", "AK"))
    override val user: StateFlow<User> = _user.asStateFlow()

    private val _lists = MutableStateFlow(seedLists())
    override val lists: StateFlow<List<ShoppingList>> = _lists.asStateFlow()

    private val _budget = MutableStateFlow(Budget(monthlyLimit = 500.0, spent = 327.40))
    override val budget: StateFlow<Budget> = _budget.asStateFlow()

    private val _expenses = MutableStateFlow(seedExpenses())
    override val expenses: StateFlow<List<ExpenseEntry>> = _expenses.asStateFlow()

    private val _notifications = MutableStateFlow(seedNotifications())
    override val notifications: StateFlow<List<KinboNotification>> = _notifications.asStateFlow()

    private val _weeklySpending = MutableStateFlow(
        listOf(
            WeeklySpending("Mon", 42.0), WeeklySpending("Tue", 18.5),
            WeeklySpending("Wed", 67.0), WeeklySpending("Thu", 12.0),
            WeeklySpending("Fri", 88.0), WeeklySpending("Sat", 54.5),
            WeeklySpending("Sun", 45.4),
        )
    )
    override val weeklySpending: StateFlow<List<WeeklySpending>> = _weeklySpending.asStateFlow()

    private val _favorites = MutableStateFlow(
        listOf("Weekly Groceries", "Monthly Stock", "Ramadan List", "Party List")
    )
    override val favorites: StateFlow<List<String>> = _favorites.asStateFlow()

    private val _premium = MutableStateFlow(false)
    override val premium: StateFlow<Boolean> = _premium.asStateFlow()

    override val isSynced: Boolean = false

    override fun categoryShare(): List<CategoryShare> {
        val grouped = _expenses.value.groupBy { it.category }.mapValues { it.value.sumOf { e -> e.amount } }
        return grouped.map { CategoryShare(it.key, it.value) }.sortedByDescending { it.amount }
    }

    override fun toggleFavorite(id: String) = _lists.update { lists ->
        lists.map { if (it.id == id) it.copy(favorite = !it.favorite, updatedAt = System.currentTimeMillis()) else it }
    }

    override fun archiveList(id: String) = _lists.update { lists ->
        lists.map { if (it.id == id) it.copy(archived = true, updatedAt = System.currentTimeMillis()) else it }
    }

    override fun deleteList(id: String) = _lists.update { lists -> lists.filterNot { it.id == id } }

    override fun duplicateList(id: String) = _lists.update { lists ->
        val src = lists.firstOrNull { it.id == id } ?: return@update lists
        lists + src.copy(id = UUID.randomUUID().toString(), name = "${src.name} (Copy)", createdAt = System.currentTimeMillis())
    }

    override fun renameList(id: String, newName: String) = _lists.update { lists ->
        lists.map { if (it.id == id) it.copy(name = newName, updatedAt = System.currentTimeMillis()) else it }
    }

    override fun addList(name: String, emoji: String): ShoppingList {
        val list = ShoppingList(name = name, emoji = emoji, collaborators = listOf(
            Collaborator(_user.value.name, ListRole.Owner, _user.value.initials, 0)
        ))
        _lists.update { listOf(list) + it }
        return list
    }

    override fun getList(id: String): ShoppingList? = _lists.value.firstOrNull { it.id == id }

    override fun addItem(listId: String, item: ShoppingItem) = _lists.update { lists ->
        lists.map { if (it.id == listId) it.copy(items = it.items + item, updatedAt = System.currentTimeMillis()) else it }
    }

    override fun updateItem(listId: String, item: ShoppingItem) = _lists.update { lists ->
        lists.map { l ->
            if (l.id == listId) l.copy(items = l.items.map { if (it.id == item.id) item else it }, updatedAt = System.currentTimeMillis()) else l
        }
    }

    override fun removeItem(listId: String, itemId: String) = _lists.update { lists ->
        lists.map { l ->
            if (l.id == listId) l.copy(items = l.items.filterNot { it.id == itemId }, updatedAt = System.currentTimeMillis()) else l
        }
    }

    override fun togglePurchased(listId: String, itemId: String) = _lists.update { lists ->
        lists.map { l ->
            if (l.id == listId) l.copy(
                items = l.items.map { if (it.id == itemId) it.copy(purchased = !it.purchased) else it },
                updatedAt = System.currentTimeMillis()
            ) else l
        }
    }

    override fun sortItemsByCategory(listId: String) = _lists.update { lists ->
        lists.map { l -> if (l.id == listId) l.copy(items = ShoppingAssistant.sortByCategory(l.items)) else l }
    }

    override fun setBudget(limit: Double) {
        _budget.value = _budget.value.copy(monthlyLimit = limit)
    }

    override fun markAllNotificationsRead() = _notifications.update { it.map { n -> n.copy(read = true) } }

    override fun login(email: String) { _user.value = _user.value.copy(email = email, initials = email.take(2).uppercase()) }
    override fun signup(name: String, email: String) { _user.value = User(name, email, name.take(2).uppercase()) }

    override fun setPremium(v: Boolean) { _premium.value = v; _user.value = _user.value.copy(premium = v, plan = if (v) "Premium" else "Free") }

    // ---- Collaboration (local stubs; real sync via FirestoreSyncRepository) ----
    override fun generateShareCode(listId: String): String {
        val code = (1..6).map { ('A'..'Z') + ('0'..'9') }.map { it.random() }.joinToString("")
        _lists.update { lists -> lists.map { if (it.id == listId) it.copy(shareCode = code, updatedAt = System.currentTimeMillis()) else it } }
        return code
    }

    override fun joinListByShareCode(shareCode: String): ShoppingList? {
        return _lists.value.firstOrNull { it.shareCode.equals(shareCode, ignoreCase = true) }
    }

    override fun addCollaborator(listId: String, collaborator: Collaborator) = _lists.update { lists ->
        lists.map { if (it.id == listId) it.copy(collaborators = (it.collaborators + collaborator).distinctBy { c -> c.userId.ifEmpty { c.name } }, updatedAt = System.currentTimeMillis()) else it }
    }

    override fun removeCollaborator(listId: String, userId: String) = _lists.update { lists ->
        lists.map { if (it.id == listId) it.copy(collaborators = it.collaborators.filterNot { it.userId == userId }, updatedAt = System.currentTimeMillis()) else it }
    }

    override fun updateCollaboratorRole(listId: String, userId: String, role: ListRole) = _lists.update { lists ->
        lists.map { if (it.id == listId) it.copy(collaborators = it.collaborators.map { if (it.userId == userId) it.copy(role = role) else it }, updatedAt = System.currentTimeMillis()) else it }
    }

    // ---- Seed data ---------------------------------------------------------
    private fun seedLists(): List<ShoppingList> {
        val weekly = ShoppingList(
            id = "weekly", name = "Weekly Groceries", emoji = "🥬", favorite = true,
            collaborators = listOf(
                Collaborator("Aisha Khan", ListRole.Owner, "AK", 0),
                Collaborator("Bilal Khan", ListRole.Editor, "BK", 1),
            ),
            items = listOf(
                ShoppingItem(name = "Milk", quantity = 2.0, unit = "L", price = 1.9, category = ItemCategory.Dairy, priority = Priority.High),
                ShoppingItem(name = "Eggs", quantity = 12.0, unit = "pcs", price = 0.25, category = ItemCategory.Dairy, priority = Priority.High, purchased = true),
                ShoppingItem(name = "Bananas", quantity = 6.0, unit = "pcs", price = 0.3, category = ItemCategory.Produce, priority = Priority.Medium, purchased = true),
                ShoppingItem(name = "Bread", quantity = 1.0, unit = "loaf", price = 1.9, category = ItemCategory.Bakery, priority = Priority.Medium),
                ShoppingItem(name = "Chicken Breast", quantity = 1.0, unit = "kg", price = 6.5, category = ItemCategory.Meat, priority = Priority.High),
                ShoppingItem(name = "Tomatoes", quantity = 4.0, unit = "pcs", price = 0.35, category = ItemCategory.Produce, priority = Priority.Medium),
                ShoppingItem(name = "Rice", quantity = 2.0, unit = "kg", price = 3.0, category = ItemCategory.Pantry, priority = Priority.Low),
            ),
        )
        val party = ShoppingList(
            id = "party", name = "Weekend Party", emoji = "🎉", favorite = true,
            collaborators = listOf(
                Collaborator("Aisha Khan", ListRole.Owner, "AK", 0),
                Collaborator("Sara Ali", ListRole.Editor, "SA", 2),
                Collaborator("Omar", ListRole.Viewer, "OM", 3),
            ),
            items = listOf(
                ShoppingItem(name = "Chips", quantity = 3.0, unit = "bags", price = 1.8, category = ItemCategory.Snacks, priority = Priority.Medium, purchased = true),
                ShoppingItem(name = "Soda", quantity = 6.0, unit = "bottles", price = 1.2, category = ItemCategory.Beverages, priority = Priority.High),
                ShoppingItem(name = "Ice Cream", quantity = 2.0, unit = "tubs", price = 4.5, category = ItemCategory.Frozen, priority = Priority.Medium),
                ShoppingItem(name = "Pizza Base", quantity = 4.0, unit = "pcs", price = 1.5, category = ItemCategory.Bakery, priority = Priority.High),
            ),
        )
        val pantry = ShoppingList(
            id = "pantry", name = "Pantry Restock", emoji = "🥫",
            collaborators = listOf(Collaborator("Aisha Khan", ListRole.Owner, "AK", 0)),
            items = listOf(
                ShoppingItem(name = "Cooking Oil", quantity = 1.0, unit = "L", price = 4.5, category = ItemCategory.Pantry, priority = Priority.High, purchased = true),
                ShoppingItem(name = "Flour", quantity = 2.0, unit = "kg", price = 1.2, category = ItemCategory.Pantry, priority = Priority.Low),
                ShoppingItem(name = "Sugar", quantity = 1.0, unit = "kg", price = 1.5, category = ItemCategory.Pantry, priority = Priority.Low),
                ShoppingItem(name = "Salt", quantity = 1.0, unit = "pcs", price = 0.6, category = ItemCategory.Pantry, priority = Priority.Low, purchased = true),
                ShoppingItem(name = "Spices", quantity = 1.0, unit = "set", price = 3.0, category = ItemCategory.Pantry, priority = Priority.Medium),
            ),
        )
        return listOf(weekly, party, pantry)
    }

    private fun seedExpenses(): List<ExpenseEntry> = listOf(
        ExpenseEntry(amount = 62.0, category = ItemCategory.Produce, listName = "Weekly Groceries"),
        ExpenseEntry(amount = 48.5, category = ItemCategory.Dairy, listName = "Weekly Groceries"),
        ExpenseEntry(amount = 35.0, category = ItemCategory.Meat, listName = "Weekly Groceries"),
        ExpenseEntry(amount = 72.0, category = ItemCategory.Beverages, listName = "Weekend Party"),
        ExpenseEntry(amount = 28.0, category = ItemCategory.Snacks, listName = "Weekend Party"),
        ExpenseEntry(amount = 41.9, category = ItemCategory.Pantry, listName = "Pantry Restock"),
        ExpenseEntry(amount = 40.0, category = ItemCategory.Household, listName = "Pantry Restock"),
    )

    private fun seedNotifications(): List<KinboNotification> = listOf(
        KinboNotification("Milk running low", "Your weekly list suggests restocking milk.", "2m ago", "🥛"),
        KinboNotification("Shared list updated", "Bilal added 'Chicken Breast' to Weekly Groceries.", "15m ago", "👥"),
        KinboNotification("Shopping reminder", "Weekend Party list is 50% complete.", "1h ago", "⏰"),
        KinboNotification("Budget update", "You've spent 65% of your monthly budget.", "3h ago", "💰"),
    )
}
