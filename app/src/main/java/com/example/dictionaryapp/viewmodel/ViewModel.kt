package com.example.dictionaryapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionaryapp.model.DictionaryApi
import com.example.dictionaryapp.model.WordDefinition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

sealed interface DictionaryUiState {
    object Idle : DictionaryUiState
    object Loading : DictionaryUiState
    data class Success(val words: List<WordDefinition>) : DictionaryUiState
    object Error : DictionaryUiState
}

class DictionaryViewModel : ViewModel() {
    var dictionaryUiState by mutableStateOf<DictionaryUiState>(DictionaryUiState.Idle)
        private set

    private val _words = MutableStateFlow<List<WordDefinition>>(emptyList())

    fun fetchWordDefinition(word: String) {
        dictionaryUiState = DictionaryUiState.Loading

        println("Loading started...")
        viewModelScope.launch {
            //delay(5000)  // slow API response
            try {
                val result = DictionaryApi.getInstance().getWordDefinition(word)
                dictionaryUiState = DictionaryUiState.Success(result) // Show success state
                println("Data loaded successfully!")
            } catch (e: Exception) {
                dictionaryUiState = DictionaryUiState.Error  // Show error state
                println("Error occurred: ${e.message}")
            }
        }
    }
}
