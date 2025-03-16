package com.example.dictionaryapp.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// Data class to match Dictionary API response
data class WordDefinition(
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>?,
    val meanings: List<Meaning>
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class Definition(
    val definition: String,
    val example: String?
)

data class Phonetic(
    val text: String?,
    val audio: String?
)

// API URL
const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/"

interface DictionaryApi {
    @GET("{word}")
    suspend fun getWordDefinition(@Path("word") word: String): List<WordDefinition>

    companion object {
        private var dictionaryService: DictionaryApi? = null

        suspend fun getInstance(): DictionaryApi {
            if (dictionaryService == null) {
                dictionaryService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(DictionaryApi::class.java)
            }
            return dictionaryService!!
        }
    }
}
