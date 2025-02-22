package com.example.http_project.remote

import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonListResponse
import com.example.http_project.model.PokemonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * RemoteDataSource is responsible for all HTTP requests to PokeAPI using HttpURLConnection,
 * parsing JSON responses manually (org.json).
 */
class RemoteDataSource {

    /**
     * Fetches a page of Pokemon, e.g. offset=0, limit=20 => https://pokeapi.co/api/v2/pokemon?offset=0&limit=20
     *
     * @param offset The index from where to start fetching.
     * @param limit Number of items to fetch, usually 20.
     * @return A [PokemonListResponse] with count, next, previous, and results (name, url).
     */
    suspend fun fetchPokemonPage(offset: Int, limit: Int): PokemonListResponse = withContext(Dispatchers.IO) {
        val apiUrl = "https://pokeapi.co/api/v2/pokemon?offset=$offset&limit=$limit"
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(apiUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 10_000
            urlConnection.readTimeout = 10_000

            urlConnection.connect()
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = urlConnection.inputStream.bufferedReader().use { it.readText() }
                parsePokemonListJson(responseString)
            } else {
                val errorMsg = urlConnection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("Error fetching Pokemon page. Code: ${urlConnection.responseCode}. Message: $errorMsg")
            }
        } finally {
            urlConnection?.disconnect()
        }
    }

    /**
     * Fetches a Pokemon detail by its direct URL (from the list).
     * Example: https://pokeapi.co/api/v2/pokemon/1/
     *
     * @return A [PokemonDetailResponse].
     */
    suspend fun fetchPokemonDetail(detailUrl: String): PokemonDetailResponse = withContext(Dispatchers.IO) {
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(detailUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 10_000
            urlConnection.readTimeout = 10_000

            urlConnection.connect()
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = urlConnection.inputStream.bufferedReader().use { it.readText() }
                parsePokemonDetailJson(responseString)
            } else {
                val errorMsg = urlConnection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("Error fetching Pokemon detail. Code: ${urlConnection.responseCode}. Message: $errorMsg")
            }
        } finally {
            urlConnection?.disconnect()
        }
    }

    /**
     * Fetches a Pokemon detail by name or ID directly from PokeAPI.
     * e.g.: https://pokeapi.co/api/v2/pokemon/pikachu or .../pokemon/25
     * This only works if the user input is an exact match.
     *
     * @param query The name or ID entered by the user.
     * @return A [PokemonDetailResponse] if found. Otherwise, throws an exception.
     */
    suspend fun fetchPokemonByNameOrId(query: String): PokemonDetailResponse = withContext(Dispatchers.IO) {
        val apiUrl = "https://pokeapi.co/api/v2/pokemon/$query"
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(apiUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 10_000
            urlConnection.readTimeout = 10_000

            urlConnection.connect()
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = urlConnection.inputStream.bufferedReader().use { it.readText() }
                parsePokemonDetailJson(responseString)
            } else {
                val errorMsg = urlConnection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("Error searching by name/ID. Code: ${urlConnection.responseCode}. Message: $errorMsg")
            }
        } finally {
            urlConnection?.disconnect()
        }
    }

    /**
     * Parses JSON for a Pokemon list.
     */
    private fun parsePokemonListJson(jsonString: String): PokemonListResponse {
        val json = JSONObject(jsonString)
        val count = json.getInt("count")
        val next = if (!json.isNull("next")) json.getString("next") else null
        val previous = if (!json.isNull("previous")) json.getString("previous") else null

        val resultsArray = json.getJSONArray("results")
        val results = mutableListOf<PokemonResult>()
        for (i in 0 until resultsArray.length()) {
            val item = resultsArray.getJSONObject(i)
            val name = item.getString("name")
            val url = item.getString("url")
            results.add(PokemonResult(name = name, url = url))
        }
        return PokemonListResponse(count, next, previous, results)
    }

    /**
     * Parses JSON for a single Pokemon detail.
     */
    private fun parsePokemonDetailJson(jsonString: String): PokemonDetailResponse {
        val json = JSONObject(jsonString)
        val id = json.getInt("id")
        val name = json.getString("name")
        val height = json.getInt("height")
        val weight = json.getInt("weight")

        val typesList = mutableListOf<String>()
        val typesArray = json.getJSONArray("types")
        for (i in 0 until typesArray.length()) {
            val typeObject = typesArray.getJSONObject(i).getJSONObject("type")
            val typeName = typeObject.getString("name")
            typesList.add(typeName)
        }

        val spritesObj = json.getJSONObject("sprites")
        val frontDefault = if (!spritesObj.isNull("front_default")) spritesObj.getString("front_default") else ""

        return PokemonDetailResponse(
            id = id,
            name = name,
            height = height,
            weight = weight,
            types = typesList,
            spriteUrl = frontDefault
        )
    }
}
