package com.example.data.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class WikidataApiService(
    private val httpClient: HttpClient
) {
    private val endpoint = "https://query.wikidata.org/sparql"

    suspend fun fetchSparqlResults(query: String): String {
        val response = httpClient.get(endpoint) {
            parameter("query", query)
            parameter("format", "json")
            headers {
                append("User-Agent", "AtlasApp/1.0 (Android GIS Engine)")
                append("Accept", "application/sparql-results+json")
            }
        }
        return response.bodyAsText()
    }
}
