package com.example.data.network

import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest
import kotlinx.serialization.json.*

object WikidataParser {
    fun parseWikidataJson(json: String, layerType: AtlasLayerType): List<AtlasPointOfInterest> {
        val jsonElement = Json.parseToJsonElement(json)
        val bindings = jsonElement.jsonObject["results"]?.jsonObject?.get("bindings")?.jsonArray ?: return emptyList()

        return bindings.mapNotNull { binding ->
            val b = binding.jsonObject
            val id = b["item"]?.jsonObject?.get("value")?.jsonPrimitive?.content?.split("/")?.lastOrNull() ?: return@mapNotNull null
            val title = b["itemLabel"]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: "Unknown"
            val coordString = b["coord"]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: return@mapNotNull null
            
            // Format is Point(lon lat)
            val coords = coordString.removePrefix("Point(").removeSuffix(")").split(" ")
            if (coords.size < 2) return@mapNotNull null
            val lon = coords[0].toDoubleOrNull() ?: return@mapNotNull null
            val lat = coords[1].toDoubleOrNull() ?: return@mapNotNull null

            AtlasPointOfInterest(
                id = id,
                title = title,
                description = b["article"]?.jsonObject?.get("value")?.jsonPrimitive?.content ?: "",
                layerType = layerType,
                latitude = lat,
                longitude = lon
            )
        }
    }
}
