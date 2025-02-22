# Pokemon_HttpURLConnection

Overview

Http_project is a modern Android application designed to fetch and display detailed Pokémon information using PokeAPI. This implementation relies on HttpURLConnection for network calls and org.json for manual JSON parsing. The app follows the MVVM (Model-View-ViewModel) architecture and leverages Jetpack Compose with Material3 for a sleek, responsive UI.

Features

Paginated Pokémon List – Browse Pokémon in a seamless, paginated format.
Detailed Pokémon View – Get in-depth details, including ID, name, height, weight, types, and sprite images.
Search Functionality – Look up Pokémon by exact name or ID for quick access.
Robust Error Handling – Gracefully manage network failures and display user-friendly error messages.
Modern UI Design – Built with Jetpack Compose and Material3, ensuring a smooth and intuitive experience.


Architecture


The project is structured following MVVM principles, making it modular and scalable:

RemoteDataSource – Handles network requests via HttpURLConnection.
Repository – Acts as a bridge between the ViewModel and data sources.
ViewModel – Manages UI state, pagination, search logic, and detailed views.
UI Components – Built with Composable functions to create an intuitive user interface.
Requirements
Android Studio (Arctic Fox or newer)
Minimum SDK: 21
Internet connection required
How to Run
Clone the repository.
Open the project in Android Studio.
Build and run the app on an emulator or a physical device.
Dependencies
The project utilizes the following libraries and tools:

org.json – For efficient JSON parsing.
Kotlin Coroutines – For asynchronous programming.
Jetpack Compose & Material3 – For modern UI development.
License
[Insert License Information]


Acknowledgments


Special thanks to PokeAPI for providing the Pokémon data.
