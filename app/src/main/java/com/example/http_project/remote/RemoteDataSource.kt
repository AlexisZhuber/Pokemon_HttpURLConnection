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
 * RemoteDataSource is responsible for making HTTP requests to the PokeAPI
 * and parsing JSON responses manually using org.json.
 *
 * This class provides methods to fetch:
 * 1) The main list of Pokemons.
 * 2) The detail of a single Pokemon, including basic info and sprite URL.
 */
class RemoteDataSource {

    /**
     * Fetches a list of Pokemon from the PokeAPI main endpoint:
     * https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0
     *
     * @return A [PokemonListResponse] containing the total count, next/previous links,
     *         and a list of [PokemonResult] items (name, url).
     * @throws Exception If there's a network error or non-OK HTTP response.
     */
    suspend fun fetchPokemons(): PokemonListResponse = withContext(Dispatchers.IO) {
        val apiUrl = "https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0"
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(apiUrl)
            urlConnection = url.openConnection() as HttpURLConnection

            // Basic HTTP configuration
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 10_000
            urlConnection.readTimeout = 10_000

            // Establish the connection
            urlConnection.connect()

            // Check the response code
            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = urlConnection.inputStream.bufferedReader().use { it.readText() }
                parsePokemonListJson(responseString)
            } else {
                val errorMsg = urlConnection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("Error fetching Pokemons. Code: $responseCode. Message: $errorMsg")
            }
        } finally {
            // Always disconnect to release resources
            urlConnection?.disconnect()
        }
    }

    /**
     * Fetches the detail of a single Pokemon using its 'url' from the Pokemon list.
     * Example of detailUrl: https://pokeapi.co/api/v2/pokemon/1/ for Bulbasaur.
     *
     * @param detailUrl The full URL to the Pokemon detail endpoint.
     * @return A [PokemonDetailResponse] with id, name, height, weight, types, and spriteUrl.
     * @throws Exception If there's a network error or non-OK HTTP response.
     */
    suspend fun fetchPokemonDetail(detailUrl: String): PokemonDetailResponse = withContext(Dispatchers.IO) {
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(detailUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 10_000
            urlConnection.readTimeout = 10_000

            // Establish the connection
            urlConnection.connect()

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseString = urlConnection.inputStream.bufferedReader().use { it.readText() }
                parsePokemonDetailJson(responseString)
            } else {
                val errorMsg = urlConnection.errorStream?.bufferedReader()?.use { it.readText() }
                throw Exception("Error fetching Pokemon detail. Code: $responseCode. Message: $errorMsg")
            }
        } finally {
            urlConnection?.disconnect()
        }
    }

    /**
     * Parses the JSON string returned for the Pokemon list endpoint.
     *
     * @param jsonString The raw JSON response.
     * @return A [PokemonListResponse] with count, next, previous, and a list of results.
     */
    private fun parsePokemonListJson(jsonString: String): PokemonListResponse {
        val json = JSONObject(jsonString)

        val count = json.getInt("count")
        val next = if (!json.isNull("next")) json.getString("next") else null
        val previous = if (!json.isNull("previous")) json.getString("previous") else null

        // 'results' is an array of objects, each containing 'name' and 'url'
        val resultsArray = json.getJSONArray("results")
        val results = mutableListOf<PokemonResult>()
        for (i in 0 until resultsArray.length()) {
            val item = resultsArray.getJSONObject(i)
            val name = item.getString("name")
            val url = item.getString("url")
            results.add(PokemonResult(name = name, url = url))
        }

        return PokemonListResponse(
            count = count,
            next = next,
            previous = previous,
            results = results
        )
    }

    /**
     * Parses the JSON string for a single Pokemon detail.
     *
     * Example partial JSON (https://pokeapi.co/api/v2/pokemon/1):
     * {
     *   "id": 1,
     *   "name": "bulbasaur",
     *   "height": 7,
     *   "weight": 69,
     *   "types": [
     *     {
     *       "slot": 1,
     *       "type": { "name": "grass", "url": "..." }
     *     },
     *     {
     *       "slot": 2,
     *       "type": { "name": "poison", "url": "..." }
     *     }
     *   ],
     *   "sprites": {
     *     "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
     *   }
     * }
     *
     * @param jsonString The raw JSON response for a single Pokemon.
     * @return A [PokemonDetailResponse] with the extracted data fields.
     */
    private fun parsePokemonDetailJson(jsonString: String): PokemonDetailResponse {
        val json = JSONObject(jsonString)

        // Basic fields
        val id = json.getInt("id")
        val name = json.getString("name")
        val height = json.getInt("height")
        val weight = json.getInt("weight")

        // Parse types
        val typesList = mutableListOf<String>()
        val typesArray = json.getJSONArray("types")
        for (i in 0 until typesArray.length()) {
            val typeObject = typesArray.getJSONObject(i).getJSONObject("type")
            val typeName = typeObject.getString("name")
            typesList.add(typeName)
        }

        // Parse sprite URL (if available)
        val spritesObj = json.getJSONObject("sprites")
        val frontDefault = if (!spritesObj.isNull("front_default")) {
            spritesObj.getString("front_default")
        } else {
            ""
        }

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
