package com.example.http_project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonResult
import com.example.http_project.ui.theme.BackgroundColor
import com.example.http_project.ui.theme.CardColor
import com.example.http_project.ui.theme.ErrorColor
import com.example.http_project.ui.theme.PrimaryColor
import com.example.http_project.ui.theme.TextColor
import com.example.http_project.util.getTypeBackgroundBrush
import com.example.http_project.viewmodel.PokemonViewModel

/**
 * Main view that displays the Pokemon list and manages
 * the state of selected Pokemon to show in a detail dialog.
 *
 * Now includes a search bar to filter the local list by name or ID.
 */
@Composable
fun PokemonListView(
    viewModel: PokemonViewModel,
    paddingValues: PaddingValues
) {
    // Observe states from ViewModel
    val pokemonListResponse by viewModel.pokemonListState.collectAsState()
    val selectedPokemonDetail by viewModel.selectedPokemonDetail.collectAsState()
    val errorMessage by viewModel.errorState.collectAsState()

    // Text state for search
    var searchText by remember { mutableStateOf("") }

    // Fetch the pokemon list when entering this screen
    LaunchedEffect(Unit) {
        viewModel.fetchPokemonList()
    }

    // UI content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BackgroundColor) // Apply background color
    ) {
        // If there's an error, display it in the center
        errorMessage?.let { error ->
            Text(
                text = "Error: $error",
                color = ErrorColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // If the list is null and there's no error, show a loading indicator
        if (pokemonListResponse == null && errorMessage == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryColor
            )
        } else {
            // If we have a list of Pokemons, show the search bar + filtered list
            pokemonListResponse?.results?.let { results ->
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    SearchBar(
                        searchText = searchText,
                        onSearchTextChange = { newText ->
                            searchText = newText
                        }
                    )

                    // Filter the Pokemon list
                    val filteredPokemons = filterPokemonsBySearch(
                        pokemons = results,
                        query = searchText
                    )

                    // Display the filtered list
                    PokemonList(
                        pokemons = filteredPokemons,
                        onItemClick = { pokemon ->
                            viewModel.fetchPokemonDetail(pokemon.url)
                        }
                    )
                }
            }
        }
    }

    // Show detail dialog if we have a selected Pokemon
    selectedPokemonDetail?.let { detail ->
        PokemonDetailDialog(
            detail = detail,
            onClose = { viewModel.closeDetailDialog() }
        )
    }
}

/**
 * Renders a simple search bar using OutlinedTextField.
 * The user can type a name or ID for filtering.
 */
@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        label = { Text("Search by name or ID") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

/**
 * Filters a list of Pokemon by name or ID.
 * - If the user enters digits, we parse them as an ID.
 * - Otherwise, we do a contains() match by name.
 */
fun filterPokemonsBySearch(
    pokemons: List<PokemonResult>,
    query: String
): List<PokemonResult> {
    if (query.isBlank()) return pokemons

    val trimmed = query.trim().lowercase()

    // If the user typed a number, try to match ID
    val possibleId = trimmed.toIntOrNull()
    return if (possibleId != null) {
        // Filter by ID
        pokemons.filter { pokemon ->
            parseIdFromUrl(pokemon.url) == possibleId
        }
    } else {
        // Filter by name containing the query
        pokemons.filter { pokemon ->
            pokemon.name.contains(trimmed, ignoreCase = true)
        }
    }
}

/**
 * Renders a lazy column of Pokemon items.
 *
 * @param pokemons The list of Pokemon results from the API.
 * @param onItemClick Callback when a user clicks on a pokemon item.
 */
@Composable
fun PokemonList(
    pokemons: List<PokemonResult>,
    onItemClick: (PokemonResult) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp) // small padding around the entire list
    ) {
        items(pokemons) { pokemon ->
            // Each item is clickable to load detail
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(pokemon) },
                colors = CardDefaults.cardColors(
                    containerColor = CardColor
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Text(
                    text = pokemon.name.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = TextColor,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * AlertDialog showing a Pokemon's basic details,
 * including an image loaded by Coil, centered for a nice appearance.
 *
 * The background depends on the Pokemon's types (solid or gradient).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailDialog(
    detail: PokemonDetailResponse,
    onClose: () -> Unit
) {
    // Brush for background based on Pokemon types
    val backgroundBrush = getTypeBackgroundBrush(detail.types)

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            // Centered title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = detail.name.uppercase(),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .background(backgroundBrush)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Sprite image
                    AsyncImage(
                        model = detail.spriteUrl,
                        contentDescription = detail.name,
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "ID: ${detail.id}", color = White)
                    Text(text = "Height: ${detail.height}", color = White)
                    Text(text = "Weight: ${detail.weight}", color = White)
                    Text(
                        text = "Types: ${detail.types.joinToString(", ")}",
                        color = White
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}

/**
 * Extract the Pokemon ID from the PokeAPI URL.
 * Example: "https://pokeapi.co/api/v2/pokemon/25/" -> returns 25
 */
fun parseIdFromUrl(url: String): Int? {
    val regex = Regex(".*/(\\d+)/?")
    val matchResult = regex.find(url) ?: return null
    return matchResult.groupValues.getOrNull(1)?.toIntOrNull()
}
