package com.example.http_project.util

import androidx.compose.ui.graphics.Brush
import com.example.http_project.ui.theme.DefaultTypeColor
import com.example.http_project.ui.theme.TypeColorMap

/**
 * Returns a [Brush] for the Pokemon card background based on the list of types.
 *
 * If there is only one type, it returns a solid color brush.
 * If there are multiple types, it returns a simple horizontal gradient brush
 * merging the corresponding colors.
 */
fun getTypeBackgroundBrush(types: List<String>): Brush {
    // Convert type strings to Colors using our map
    val typeColors = types.map { typeName ->
        TypeColorMap[typeName] ?: DefaultTypeColor
    }

    return when (typeColors.size) {
        0 -> Brush.linearGradient(listOf(DefaultTypeColor, DefaultTypeColor)) // fallback
        1 -> Brush.linearGradient(listOf(typeColors[0], typeColors[0]))      // solid
        else -> Brush.horizontalGradient(typeColors)                         // gradient if 2+ types
    }
}
