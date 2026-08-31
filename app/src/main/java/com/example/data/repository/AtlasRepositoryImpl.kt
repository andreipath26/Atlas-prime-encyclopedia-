package com.example.data.repository

import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest
import com.example.data.network.WikidataApiService
import com.example.data.network.WikidataParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AtlasRepositoryImpl(
    private val apiService: WikidataApiService
) : AtlasRepository {

    override suspend fun fetchLayerPoints(layerType: AtlasLayerType): Result<List<AtlasPointOfInterest>> = 
        withContext(Dispatchers.IO) {
            runCatching {
                val query = buildSparqlQuery(layerType)
                val rawJson = apiService.fetchSparqlResults(query)
                WikidataParser.parseWikidataJson(rawJson, layerType)
            }
        }

    private fun buildSparqlQuery(layerType: AtlasLayerType): String {
        val typeIds = when (layerType) {
            AtlasLayerType.HISTORY -> "wd:Q839954 wd:Q1081138" // Archaeological sites & monuments
            AtlasLayerType.BOTANY -> "wd:Q22652 wd:Q260271"    // Botanical gardens & arboretums
            AtlasLayerType.SCIENCE -> "wd:Q125501 wd:Q33442"    // Observatories & particle colliders
            else -> "wd:Q107452" // Default fallback for Geography/Culture
        }

        return """
            SELECT ?item ?itemLabel ?coord ?image ?article WHERE {
              ?item wdt:P31/wdt:279* ?type .
              VALUES ?type { $typeIds }
              ?item wdt:P625 ?coord .
              OPTIONAL { ?item wdt:P18 ?image . }
              OPTIONAL {
                ?article schema:about ?item ;
                         schema:isPartOf <https://en.wikipedia.org/> .
              }
              SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
            }
            LIMIT 300
        """.trimIndent()
    }
}
