package com.kinbo.app.data

import com.kinbo.app.model.ItemCategory
import com.kinbo.app.model.Priority
import com.kinbo.app.model.ShoppingItem

/**
 * Rule-based shopping assistant. Mirrors the PRD "AI Shopping Assistant" feature:
 * given an item, suggests commonly-paired groceries.
 */
object ShoppingAssistant {

    private val pairings: Map<String, List<Pair<String, ItemCategory>>> = mapOf(
        "chicken" to listOf(
            "Onion" to ItemCategory.Produce, "Garlic" to ItemCategory.Produce,
            "Ginger" to ItemCategory.Produce, "Cooking Oil" to ItemCategory.Pantry,
            "Chili" to ItemCategory.Produce, "Rice" to ItemCategory.Pantry,
        ),
        "pasta" to listOf(
            "Tomato Sauce" to ItemCategory.Pantry, "Garlic" to ItemCategory.Produce,
            "Parmesan" to ItemCategory.Dairy, "Olive Oil" to ItemCategory.Pantry,
            "Basil" to ItemCategory.Produce,
        ),
        "milk" to listOf(
            "Cereal" to ItemCategory.Pantry, "Eggs" to ItemCategory.Dairy,
            "Bread" to ItemCategory.Bakery, "Coffee" to ItemCategory.Beverages,
        ),
        "coffee" to listOf(
            "Sugar" to ItemCategory.Pantry, "Milk" to ItemCategory.Dairy,
            "Cream" to ItemCategory.Dairy, "Cookies" to ItemCategory.Snacks,
        ),
        "eggs" to listOf(
            "Bread" to ItemCategory.Bakery, "Butter" to ItemCategory.Dairy,
            "Cheese" to ItemCategory.Dairy, "Tomato" to ItemCategory.Produce,
        ),
        "rice" to listOf(
            "Lentils" to ItemCategory.Pantry, "Cooking Oil" to ItemCategory.Pantry,
            "Onion" to ItemCategory.Produce, "Spices" to ItemCategory.Pantry,
        ),
        "bread" to listOf(
            "Butter" to ItemCategory.Dairy, "Eggs" to ItemCategory.Dairy,
            "Jam" to ItemCategory.Pantry, "Cheese" to ItemCategory.Dairy,
        ),
        "fish" to listOf(
            "Lemon" to ItemCategory.Produce, "Garlic" to ItemCategory.Produce,
            "Butter" to ItemCategory.Dairy, "Herbs" to ItemCategory.Produce,
        ),
        "beef" to listOf(
            "Potato" to ItemCategory.Produce, "Onion" to ItemCategory.Produce,
            "Pepper" to ItemCategory.Produce, "Cooking Oil" to ItemCategory.Pantry,
        ),
        "tea" to listOf(
            "Sugar" to ItemCategory.Pantry, "Biscuits" to ItemCategory.Snacks,
            "Honey" to ItemCategory.Pantry, "Lemon" to ItemCategory.Produce,
        ),
    )

    /** Suggestions for a typed item, excluding items already present. */
    fun suggest(query: String, existing: List<ShoppingItem>): List<ShoppingItem> {
        val key = query.trim().lowercase()
        if (key.isBlank()) return seasonalRecommendations()
        val existingNames = existing.map { it.name.lowercase() }.toSet()
        val recipe = pairings.entries.firstOrNull { key.contains(it.key) }?.value
            ?: return seasonalRecommendations().filter { it.name.lowercase() !in existingNames }
        return recipe
            .filter { it.first.lowercase() !in existingNames }
            .map { (name, cat) ->
                ShoppingItem(name = name, category = cat, priority = Priority.Low, price = guessPrice(name))
            }
    }

    fun seasonalRecommendations(): List<ShoppingItem> = listOf(
        ShoppingItem(name = "Pumpkin", category = ItemCategory.Produce, price = 3.5, priority = Priority.Low),
        ShoppingItem(name = "Cinnamon", category = ItemCategory.Pantry, price = 2.8, priority = Priority.Low),
        ShoppingItem(name = "Hot Chocolate", category = ItemCategory.Beverages, price = 4.2, priority = Priority.Low),
        ShoppingItem(name = "Soup Mix", category = ItemCategory.Pantry, price = 2.1, priority = Priority.Low),
    )

    fun recipeIdeas(items: List<ShoppingItem>): List<String> {
        val names = items.map { it.name.lowercase() }
        val ideas = mutableListOf<String>()
        if (names.any { it.contains("chicken") }) ideas += "Chicken Stir-Fry with Rice"
        if (names.any { it.contains("pasta") } || (names.any { it.contains("tomato") } && names.any { it.contains("garlic") }))
            ideas += "Classic Tomato Pasta"
        if (names.any { it.contains("egg") } && names.any { it.contains("bread") }) ideas += "Egg & Cheese Sandwich"
        if (names.any { it.contains("fish") }) ideas += "Garlic Butter Fish"
        if (ideas.isEmpty()) ideas += "Quick Veggie Omelette"
        return ideas
    }

    fun estimateBill(items: List<ShoppingItem>): Double = items.sumOf { it.price * it.quantity }

    fun detectDuplicates(items: List<ShoppingItem>): List<String> {
        return items.groupingBy { it.name.lowercase() }
            .eachCount().filter { it.value > 1 }.keys.toList()
    }

    /** Smart category sorting: by category then priority. */
    fun sortByCategory(items: List<ShoppingItem>): List<ShoppingItem> {
        val priorityOrder = listOf(Priority.High, Priority.Medium, Priority.Low)
        return items.sortedWith(compareBy({ it.category.ordinal }, { priorityOrder.indexOf(it.priority) }))
    }

    private val priceMap = mapOf(
        "onion" to 1.2, "garlic" to 0.9, "ginger" to 1.5, "cooking oil" to 4.5,
        "chili" to 1.0, "rice" to 6.0, "tomato sauce" to 2.3, "parmesan" to 5.0,
        "olive oil" to 7.5, "basil" to 1.8, "cereal" to 3.8, "eggs" to 2.5,
        "bread" to 1.9, "coffee" to 6.5, "sugar" to 1.5, "cream" to 1.7,
        "cookies" to 2.6, "butter" to 2.8, "cheese" to 4.0, "tomato" to 1.1,
        "lentils" to 2.2, "spices" to 3.0, "jam" to 3.4, "lemon" to 0.7,
        "herbs" to 1.6, "potato" to 1.3, "pepper" to 1.2, "biscuits" to 2.4,
        "honey" to 5.5,
    )

    fun guessPrice(name: String): Double = priceMap[name.lowercase()] ?: 2.0
}
