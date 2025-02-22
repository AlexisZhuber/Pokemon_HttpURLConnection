package com.example.http_project.repository

import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonListResponse
import com.example.http_project.remote.RemoteDataSource

class PokemonRepository(
    private val remoteDataSource: RemoteDataSource
) {

    /**
     * Retrieves the Pokemon list from PokeAPI.
     */
    suspend fun getPokemonList(): PokemonListResponse {
        return remoteDataSource.fetchPokemons()
    }

    /**
     * Retrieves the details of a specific Pokemon by its URL.
     */
    suspend fun getPokemonDetail(url: String): PokemonDetailResponse {
        return remoteDataSource.fetchPokemonDetail(url)
    }
}
