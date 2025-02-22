package com.example.http_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonResult
import com.example.http_project.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * PokemonViewModel:
 * - Maintains a paginated list of Pokemon (20 per page).
 * - Loads next pages automatically while scrolling.
 * - Allows searching the API directly for a Pokemon by exact name/ID.
 */
class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    // Accumulated list of loaded Pokemon
    private val _allPokemon = mutableListOf<PokemonResult>()

    // Flow for UI to observe the loaded Pokemon
    private val _pokemonListState = MutableStateFlow<List<PokemonResult>>(emptyList())
    val pokemonListState: StateFlow<List<PokemonResult>> get() = _pokemonListState

    private var totalCount = 0
    private var currentOffset = 0
    private var isLoadingPage = false

    // If the user performs a search by name/ID, we store the result separately
    private val _searchResult = MutableStateFlow<PokemonDetailResponse?>(null)
    val searchResult: StateFlow<PokemonDetailResponse?> get() = _searchResult

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    // Detail of a selected Pokemon (from the list or from search)
    private val _selectedPokemonDetail = MutableStateFlow<PokemonDetailResponse?>(null)
    val selectedPokemonDetail: StateFlow<PokemonDetailResponse?> get() = _selectedPokemonDetail

    /**
     * Start from offset=0, clear the list, and load the first page.
     */
    fun fetchInitialPage() {
        if (isLoadingPage) return
        _allPokemon.clear()
        _pokemonListState.value = emptyList()
        _errorState.value = null
        currentOffset = 0
        loadNextPage()
    }

    /**
     * Loads the next page of Pokemon (20 items) automatically when user scrolls to the end.
     */
    fun loadNextPage() {
        if (isLoadingPage) return
        if (totalCount in 1..currentOffset) return

        isLoadingPage = true
        viewModelScope.launch {
            try {
                val response = repository.getPokemonPage(offset = currentOffset, limit = 20)
                totalCount = response.count

                _allPokemon.addAll(response.results)
                _pokemonListState.value = _allPokemon.toList()

                currentOffset += 20
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                isLoadingPage = false
            }
        }
    }

    /**
     * Searches for a Pokemon by exact name or ID using the PokeAPI (no partial match).
     * If found, we store it in _searchResult.
     */
    fun searchPokemon(query: String) {
        if (query.isBlank()) {
            // Clear search result
            _searchResult.value = null
            return
        }
        viewModelScope.launch {
            try {
                val detail = repository.getPokemonByNameOrId(query.trim().lowercase())
                _searchResult.value = detail
                _errorState.value = null
            } catch (e: Exception) {
                // If not found or error
                _searchResult.value = null
                //_errorState.value = e.message
                _errorState.value = null
            }
        }
    }

    /**
     * Clears the current search result. For instance, when the user erases the search text.
     */
    fun clearSearchResult() {
        _searchResult.value = null
    }

    /**
     * Handles selection of a Pokemon to show in detail dialog.
     * This can be from the paginated list or from search result.
     */
    fun fetchPokemonDetail(url: String) {
        viewModelScope.launch {
            try {
                val detail = repository.getPokemonDetail(url)
                _selectedPokemonDetail.value = detail
            } catch (e: Exception) {
                _errorState.value = e.message
            }
        }
    }

    /**
     * Closes the detail dialog by resetting the selected detail.
     */
    fun closeDetailDialog() {
        _selectedPokemonDetail.value = null
    }


    fun closeSearchAndOpenDetail(detail: PokemonDetailResponse) {
        // Clear search result
        _searchResult.value = null
        // Show detail in dialog
        _selectedPokemonDetail.value = detail
    }

}
