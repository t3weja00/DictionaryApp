package com.example.dictionaryapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dictionaryapp.ui.theme.DictionaryAppTheme
import com.example.dictionaryapp.viewmodel.DictionaryViewModel
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.Alignment
import com.example.dictionaryapp.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dictionaryapp.model.WordDefinition
import com.example.dictionaryapp.viewmodel.DictionaryUiState


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DictionaryAppTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.dictionary_app)) },
                        actions = {
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    )

                } ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dictionary"
                    )
                    {
                        composable("dictionary") { DictionaryScreen(modifier = Modifier.padding(innerPadding), dictionaryViewModel = viewModel()) }
                        composable("settings") { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}


@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    dictionaryViewModel: DictionaryViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        // TextField for searching words
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.enter_a_word)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = stringResource(id = R.string.app_name)
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Button
        Button(
            onClick = { dictionaryViewModel.fetchWordDefinition(searchQuery) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.search))
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Display word details


        // Display UI Based on State
        when (val uiState = dictionaryViewModel.dictionaryUiState) {
            is DictionaryUiState.Idle -> {
                Text("Start by searching for a word!", modifier = Modifier.padding(16.dp))
            }
            is DictionaryUiState.Loading -> LoadingScreen()
            is DictionaryUiState.Success -> WordDetails(uiState.words)
            is DictionaryUiState.Error -> ErrorScreen()
        }
    }
}

@Composable
fun WordDetails(words: List<WordDefinition>) {

    val context = LocalContext.current

    LazyColumn {
        items(words.size) { index ->
            val word = words[index]
            Text(text = "Word: ${word.word}", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Phonetic:${word.phonetic}" ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            word.phonetics?.forEach { url ->
                if (url.audio != "") {
                    val audioUrl = url.audio ?: ""
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .padding(8.dp)
                        .clickable { playAudioWithExoPlayer(context, audioUrl) }) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play Audio",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Play Audio",
                        )
                    }
                }
            }

            word.meanings.forEach { meaning ->
                Box(
                    modifier = Modifier
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = meaning.partOfSpeech,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                                modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                meaning.definitions.forEach { definition ->
                    if (definition.example != null) {
                        Text(
                            text = "Definition: ${definition.definition}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = "Example: ${definition.example}" ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                }
            }

        }

    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings Page", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Fetching results...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorScreen() {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .padding(top=4.dp,bottom=4.dp)

    ) {
        Text("Error retrieving data. Please try again.", color = MaterialTheme.colorScheme.error)
    }
}


fun playAudioWithExoPlayer(context: Context, url: String) {
    val player = ExoPlayer.Builder(context).build()
    val mediaItem = MediaItem.fromUri(url)

    player.setMediaItem(mediaItem)
    player.prepare()
    player.play()
}





