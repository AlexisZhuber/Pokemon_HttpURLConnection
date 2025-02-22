package com.example.http_project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonListResponse
import com.example.http_project.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PokemonViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    // Holds the list of pokemons
    private val _pokemonListState = MutableStateFlow<PokemonListResponse?>(null)
    val pokemonListState: StateFlow<PokemonListResponse?> get() = _pokemonListState

    // Holds the detail of the selected Pokemon
    private val _selectedPokemonDetail = MutableStateFlow<PokemonDetailResponse?>(null)
    val selectedPokemonDetail: StateFlow<PokemonDetailResponse?> get() = _selectedPokemonDetail

    // Holds any error message
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    // Load the initial list of Pokemon
    fun fetchPokemonList() {
        viewModelScope.launch {
            try {
                val response = repository.getPokemonList()
                _pokemonListState.value = response
            } catch (e: Exception) {
                _errorState.value = e.message
            }
        }
    }

    // Load the detail of a single Pokemon by URL
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

    // Close the detail dialog by clearing the selected detail
    fun closeDetailDialog() {
        _selectedPokemonDetail.value = null
    }
}
