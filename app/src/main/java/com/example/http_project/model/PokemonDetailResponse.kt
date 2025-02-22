package com.example.http_project.model

/**
 * Represents a detailed view of a Pokemon from https://pokeapi.co/api/v2/pokemon/{id}
 * We added a spriteUrl field to load an image with Coil.
 */
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<String>,
    val spriteUrl: String
)