package com.example.di

import com.example.data.network.WikidataApiService
import com.example.data.repository.AtlasRepository
import com.example.data.repository.AtlasRepositoryImpl
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object DependencyProvider {
    private val httpClient: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    val repository: AtlasRepository by lazy {
        AtlasRepositoryImpl(WikidataApiService(httpClient))
    }
}
