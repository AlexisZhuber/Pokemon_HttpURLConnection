package com.example.http_project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.http_project.model.PokemonDetailResponse
import com.example.http_project.model.PokemonResult
import com.example.http_project.ui.theme.BackgroundColor
import com.example.http_project.ui.theme.CardColor
import com.example.http_project.ui.theme.ErrorColor
import com.example.http_project.ui.theme.TextColor
import com.example.http_project.util.getTypeBackgroundBrush
import com.example.http_project.viewmodel.PokemonViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Main screen that shows:
 * 1) A search bar for exact name/ID (from PokeAPI) with a search icon button.
 * 2) If a search result is found, displays it with a full-type-color background.
 * 3) Otherwise, displays the paginated list (auto-load on scroll).
 * 4) Displays a detail dialog for any selected Pokemon, centered.
 */
@Composable
fun PokemonListView(
    viewModel: PokemonViewModel,
    paddingValues: PaddingValues
) {
    val pokemonList by viewModel.pokemonListState.collectAsState()
    val selectedPokemonDetail by viewModel.selectedPokemonDetail.collectAsState()
    val errorMessage by viewModel.errorState.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()

    // Local state for the search text
    var searchText by remember { mutableStateOf("") }

    // On first load, fetch initial page
    LaunchedEffect(Unit) {
        viewModel.fetchInitialPage()
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(BackgroundColor)
    ) {
        // If there's an error, show it centered
        errorMessage?.let { error ->
            Text(
                text = "Error: $error",
                color = ErrorColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar with a trailing icon (lupa)
            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    // We only update the text in state;
                    // search happens when user presses the icon
                    searchText = newText
                },
                label = { Text("Search Pokémon by name or ID") },
                singleLine = true, // Only one line
                trailingIcon = {
                    IconButton(onClick = {
                        // Search only when icon is pressed
                        if (searchText.isBlank()) {
                            viewModel.clearSearchResult()
                        } else {
                            viewModel.searchPokemon(searchText)
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            // If we have a search result, show it
            if (searchResult != null) {
                SearchResultCard(
                    pokemonDetail = searchResult,
                    onClick = { detail ->
                        // If user clicks on the card, open the dialog
                        viewModel.closeSearchAndOpenDetail(detail)
                    }
                )
            } else {
                // Otherwise, show the paginated list
                PaginatedPokemonList(
                    pokemonList = pokemonList,
                    onPokemonClick = { result ->
                        viewModel.fetchPokemonDetail(result.url)
                    },
                    onLoadNextPage = {
                        viewModel.loadNextPage()
                    }
                )
            }
        }
    }

    // Detail dialog if a Pokemon is selected
    selectedPokemonDetail?.let { detail ->
        PokemonDetailDialog(
            detail = detail,
            onDismiss = { viewModel.closeDetailDialog() }
        )
    }
}

/**
 * Shows a single Pokemon card with a background color/gradient
 * that covers the entire card area.
 */
@Composable
fun SearchResultCard(
    pokemonDetail: PokemonDetailResponse?,
    onClick: (PokemonDetailResponse) -> Unit
) {
    if (pokemonDetail == null) return

    val backgroundBrush = getTypeBackgroundBrush(pokemonDetail.types)

    // Card with transparent container so that the brush fully covers the card area
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(pokemonDetail) },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        // Fill the entire Card area with the type-based background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = pokemonDetail.name.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = pokemonDetail.spriteUrl,
                    contentDescription = pokemonDetail.name,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "ID: ${pokemonDetail.id}", color = White)
                Text(text = "Types: ${pokemonDetail.types.joinToString()}", color = White)
            }
        }
    }
}

/**
 * Displays a LazyColumn of PokemonResults with infinite scroll:
 * loads the next page when the user reaches the end of the list.
 */
@Composable
fun PaginatedPokemonList(
    pokemonList: List<PokemonResult>,
    onPokemonClick: (PokemonResult) -> Unit,
    onLoadNextPage: () -> Unit
) {
    val listState = rememberLazyListState()

    // Detect when we reach the end of the list to load the next page
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .filter { lastVisibleIndex ->
                lastVisibleIndex != null && lastVisibleIndex >= pokemonList.lastIndex
            }
            .collect {
                onLoadNextPage()
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pokemonList) { pokemon ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPokemonClick(pokemon) }
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
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
 * Detail dialog for the selected Pokemon, with background gradient based on types,
 * and fully centered content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailDialog(
    detail: PokemonDetailResponse,
    onDismiss: () -> Unit
) {
    val backgroundBrush = getTypeBackgroundBrush(detail.types)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(detail.name.uppercase(), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            // Center everything vertically as well
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp) // a bit more space
                    .background(backgroundBrush),
                contentAlignment = Alignment.Center // center child vertically and horizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    AsyncImage(
                        model = detail.spriteUrl,
                        contentDescription = detail.name,
                        modifier = Modifier.size(140.dp)
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
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
