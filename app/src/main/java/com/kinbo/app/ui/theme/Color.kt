package com.kinbo.app.ui.theme

import androidx.compose.ui.graphics.Color

// Kinbo brand: fresh green (groceries / fresh produce)
val KinboGreen = Color(0xFF00A86B)
val KinboGreenDark = Color(0xFF00784E)
val KinboGreenLight = Color(0xFF4FCFA3)
val KinboMint = Color(0xFFE8F7F0)

val AmberAccent = Color(0xFFFFB300)
val CoralAccent = Color(0xFFFF6B5C)
val SkyAccent = Color(0xFF29B6F6)
val PurpleAccent = Color(0xFF8E5BFF)

// Light scheme
val LightPrimary = KinboGreen
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = KinboMint
val LightOnPrimaryContainer = Color(0xFF003821)
val LightSecondary = AmberAccent
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFF1CC)
val LightOnSecondaryContainer = Color(0xFF4A3500)
val LightTertiary = SkyAccent
val LightBackground = Color(0xFFF7FAF8)
val LightOnBackground = Color(0xFF0F1A14)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F1A14)
val LightSurfaceVariant = Color(0xFFE6ECE8)
val LightOnSurfaceVariant = Color(0xFF454D49)
val LightOutline = Color(0xFFB6BFB9)

// Dark scheme
val DarkPrimary = KinboGreenLight
val DarkOnPrimary = Color(0xFF003821)
val DarkPrimaryContainer = KinboGreenDark
val DarkOnPrimaryContainer = KinboMint
val DarkSecondary = Color(0xFFFFD27A)
val DarkOnSecondary = Color(0xFF3D2A00)
val DarkSecondaryContainer = Color(0xFF583F00)
val DarkOnSecondaryContainer = Color(0xFFFFE5B0)
val DarkTertiary = Color(0xFF8AD4F5)
val DarkBackground = Color(0xFF0E1410)
val DarkOnBackground = Color(0xFFE4EAE6)
val DarkSurface = Color(0xFF161E18)
val DarkOnSurface = Color(0xFFE4EAE6)
val DarkSurfaceVariant = Color(0xFF3B443F)
val DarkOnSurfaceVariant = Color(0xFFBFC9C2)
val DarkOutline = Color(0xFF7A837E)

// Category colors used across lists
object CategoryColors {
    val Produce = Color(0xFF66BB6A)
    val Dairy = Color(0xFF42A5F5)
    val Bakery = Color(0xFFFFB74D)
    val Meat = Color(0xFFEF5350)
    val Pantry = Color(0xFFAB47BC)
    val Frozen = Color(0xFF26C6DA)
    val Beverages = Color(0xFFFF8A65)
    val Household = Color(0xFF8D6E63)
    val Snacks = Color(0xFFEC407A)
    val Other = Color(0xFF78909C)

    fun forCategory(name: String): Color = when (name.lowercase()) {
        "produce", "fruits & vegetables" -> Produce
        "dairy", "dairy & eggs" -> Dairy
        "bakery" -> Bakery
        "meat", "meat & seafood" -> Meat
        "pantry" -> Pantry
        "frozen" -> Frozen
        "beverages" -> Beverages
        "household" -> Household
        "snacks" -> Snacks
        else -> Other
    }
}
