package com.example.http_project

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.http_project.remote.RemoteDataSource
import com.example.http_project.repository.PokemonRepository
import com.example.http_project.ui.theme.Http_projectTheme
import com.example.http_project.ui.theme.PrimaryColor
import com.example.http_project.ui.theme.TextColor
import com.example.http_project.view.PokemonListView
import com.example.http_project.viewmodel.PokemonViewModel

/**
 * MainActivity sets the content to the PokemonListView.
 *
 * We add a TopAppBar with the title "Pokemon List" using Scaffold (Material3).
 * For simplicity, we are manually instantiating the dependencies
 * (RemoteDataSource, Repository, ViewModel).
 *
 * In a real project, you might use Dependency Injection (Hilt, Koin, etc.).
 */
class MainActivity : ComponentActivity() {

    private val remoteDataSource by lazy { RemoteDataSource() }
    private val repository by lazy { PokemonRepository(remoteDataSource) }
    private val viewModel by lazy { PokemonViewModel(repository) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables edge-to-edge rendering
        enableEdgeToEdge()

        setContent {
            Http_projectTheme {
                // Using Scaffold from Material3 to show a TopAppBar
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = "Pokémon List", style = MaterialTheme.typography.titleLarge, color = TextColor)
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = PrimaryColor
                            ),
                            actions = {
                                // Este composable se coloca a la derecha
                                Icon(
                                    painter = painterResource(id = R.drawable.pokeball),
                                    contentDescription = "Pokeball Icon",
                                    tint = Color.Unspecified, // Mantiene el color original
                                    modifier = Modifier.size(48.dp).padding(end = 16.dp)
                                )
                            }
                        )
                    }
                ) { padding ->
                    // The 'it' padding corresponds to scaffold content padding
                    // We can pass it along to our composable or ignore it:
                    PokemonListView(viewModel = viewModel, padding)
                }
            }
        }
    }
}
