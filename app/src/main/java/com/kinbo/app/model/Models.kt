package com.kinbo.app.model

import java.util.UUID

enum class Priority { Low, Medium, High }
enum class ListRole { Owner, Editor, Viewer }
enum class ItemCategory(val displayName: String) {
    Produce("Fruits & Vegetables"),
    Dairy("Dairy & Eggs"),
    Bakery("Bakery"),
    Meat("Meat & Seafood"),
    Pantry("Pantry"),
    Frozen("Frozen"),
    Beverages("Beverages"),
    Household("Household"),
    Snacks("Snacks"),
    Other("Other");

    companion object {
        fun fromDisplay(name: String): ItemCategory =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: Other
    }
}

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val note: String = "",
    val price: Double = 0.0,
    val category: ItemCategory = ItemCategory.Other,
    val priority: Priority = Priority.Medium,
    val purchased: Boolean = false,
)

data class Collaborator(
    val name: String,
    val role: ListRole,
    val initials: String,
    val colorIndex: Int = 0,
    val userId: String = "",
    val email: String = "",
)

data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "🛒",
    val items: List<ShoppingItem> = emptyList(),
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val collaborators: List<Collaborator> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val ownerId: String = "",
    val shareCode: String = "",
    val lastModifiedBy: String = "",
) {
    val totalItems: Int get() = items.size
    val purchasedCount: Int get() = items.count { it.purchased }
    val progress: Float get() = if (items.isEmpty()) 0f else purchasedCount.toFloat() / items.size
    val estimatedTotal: Double get() = items.sumOf { it.price * it.quantity }
    val active: Boolean get() = !archived && items.any { !it.purchased }
}

data class Budget(
    val monthlyLimit: Double,
    val spent: Double,
) {
    val remaining: Double get() = monthlyLimit - spent
    val progress: Float get() = if (monthlyLimit <= 0) 0f else (spent / monthlyLimit).toFloat().coerceIn(0f, 1f)
    val exceeded: Boolean get() = spent > monthlyLimit
}

data class ExpenseEntry(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: ItemCategory,
    val date: Long = System.currentTimeMillis(),
    val listName: String = "",
)

data class KinboNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val time: String,
    val icon: String = "🔔",
    val read: Boolean = false,
)

data class User(
    val name: String,
    val email: String,
    val initials: String,
    val plan: String = "Free",
    val premium: Boolean = false,
)

data class WeeklySpending(val dayLabel: String, val amount: Double)
data class CategoryShare(val category: ItemCategory, val amount: Double)
