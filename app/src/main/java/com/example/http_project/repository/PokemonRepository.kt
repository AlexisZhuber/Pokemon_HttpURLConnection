package com.example.http_project.repository

import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonListResponse
import com.example.http_project.remote.RemoteDataSource

/**
 * PokemonRepository acts as an intermediary between the ViewModel and the RemoteDataSource.
 * It provides methods to:
 * 1) Fetch a paginated list of Pokemon (page-based on offset and limit).
 * 2) Fetch detailed information for a single Pokemon by URL.
 * 3) Search for a Pokemon by exact name or ID.
 *
 * This approach follows the Repository pattern, isolating the data access
 * logic from the rest of the application.
 */
class PokemonRepository(
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Retrieves a page of Pokemon from the PokeAPI.
     * For example, offset=0 and limit=20 would fetch the first page of 20 Pokemon.
     *
     * @param offset The starting index in the list of Pokemons.
     * @param limit The maximum number of Pokemons to fetch in this request. Defaults to 20.
     * @return A [PokemonListResponse] containing the total count, next/previous links,
     *         and a list of [PokemonResult] items.
     * @throws Exception If there's an error fetching data (e.g. network issues).
     */
    suspend fun getPokemonPage(offset: Int, limit: Int = 20): PokemonListResponse {
        return remoteDataSource.fetchPokemonPage(offset, limit)
    }

    /**
     * Retrieves the details of a single Pokemon based on its URL.
     * Example of a detail URL: https://pokeapi.co/api/v2/pokemon/1/
     *
     * @param url The URL pointing to a specific Pokemon's details.
     * @return A [PokemonDetailResponse] with fields such as id, name, height, weight, types, etc.
     * @throws Exception If there's an error fetching the data or parsing the JSON.
     */
    suspend fun getPokemonDetail(url: String): PokemonDetailResponse {
        return remoteDataSource.fetchPokemonDetail(url)
    }

    /**
     * Searches for a Pokemon by an exact name or ID using the PokeAPI.
     * For example: "pikachu" or "25".
     *
     * This will only succeed for exact matches, since the PokeAPI does not support
     * partial or fuzzy search by default.
     *
     * @param query The exact name or ID of the Pokemon (case-insensitive).
     * @return A [PokemonDetailResponse] if a matching Pokemon is found.
     * @throws Exception If the Pokemon cannot be found or there's a network/error response.
     */
    suspend fun getPokemonByNameOrId(query: String): PokemonDetailResponse {
        return remoteDataSource.fetchPokemonByNameOrId(query)
    }
}
