package com.example.http_project.ui.theme

import androidx.compose.ui.graphics.Color

// Primary color for text or highlights
val PrimaryColor = Color(0xFF58CB5C)

// Secondary accent color
val SecondaryColor = Color(0xFF61DA6D)

// Color for card backgrounds
val CardColor = Color(0xFFABF6AF)

// Color for the main background
val BackgroundColor = Color(0xFFECEFF1)

// Error color
val ErrorColor = Color(0xFFD32F2F)

// Text color
val TextColor = Color(0xFF000000)

// Sample color constants (customize as you like)
val FireColor = Color(0xFFFFA756)
val WaterColor = Color(0xFF58ABF6)
val GrassColor = Color(0xFF8BBE8A)
val PoisonColor = Color(0xFF9F5BBA)
val ElectricColor = Color(0xFFFFCE4B)
val GroundColor = Color(0xFFD78555)
val FairyColor = Color(0xFFEE99AC)
val PsychicColor = Color(0xFFFF9BB5)
val RockColor = Color(0xFFB69E31)
val FightingColor = Color(0xFFC03028)
val GhostColor = Color(0xFF705898)
val FlyingColor = Color(0xFFA890F0)
val BugColor = Color(0xFFA8B820)
val NormalColor = Color(0xFFA8A878)
val DarkColor = Color(0xFF705848)
val SteelColor = Color(0xFFB8B8D0)
val IceColor = Color(0xFF98D8D8)
val DragonColor = Color(0xFF7038F8)
val DefaultTypeColor = Color(0xFFAAAAAA) // fallback color

/**
 * Map each known type to its corresponding color.
 * The keys match the type strings from the PokeAPI response (e.g., "fire", "water", etc.).
 */
val TypeColorMap = mapOf(
    "fire" to FireColor,
    "water" to WaterColor,
    "grass" to GrassColor,
    "poison" to PoisonColor,
    "electric" to ElectricColor,
    "ground" to GroundColor,
    "fairy" to FairyColor,
    "psychic" to PsychicColor,
    "rock" to RockColor,
    "fighting" to FightingColor,
    "ghost" to GhostColor,
    "flying" to FlyingColor,
    "bug" to BugColor,
    "normal" to NormalColor,
    "dark" to DarkColor,
    "steel" to SteelColor,
    "ice" to IceColor,
    "dragon" to DragonColor
)