package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource

// Source & Layer Identifiers
private const val ATLAS_GEOJSON_SOURCE = "atlas-poi-clustered-source"
private const val LAYER_CLUSTERS = "atlas-clusters"
private const val LAYER_CLUSTER_COUNT = "atlas-cluster-count"
private const val LAYER_UNCLUSTERED_POINTS = "atlas-unclustered-points"

private const val MAP_STYLE_DARK = "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"

@Composable
fun MapLibreClusteredView(
    activeLayers: Set<AtlasLayerType>,
    pointsOfInterest: List<AtlasPointOfInterest>,
    onPoiSelected: (AtlasPointOfInterest) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { view ->
        view.getMapAsync { map ->
            if (map.style == null) {
                map.setStyle(MAP_STYLE_DARK) { style ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(46.1748, 26.4719))
                        .zoom(5.0)
                        .build()

                    // 1. Setup Clustered GeoJson Source
                    if (style.getSource(ATLAS_GEOJSON_SOURCE) == null) {
                        val options = GeoJsonOptions()
                            .withCluster(true)
                            .withClusterMaxZoom(14) // Max zoom level to cluster points (past zoom 14, individual points render)
                            .withClusterRadius(50)  // Cluster radius in DP

                        style.addSource(GeoJsonSource(ATLAS_GEOJSON_SOURCE, options))
                    }

                    // 2. Add Cluster Circles with Color Steps based on point density
                    val clustersLayer = CircleLayer(LAYER_CLUSTERS, ATLAS_GEOJSON_SOURCE).apply {
                        setFilter(has("point_count"))
                        setProperties(
                            circleColor(
                                step(
                                    get("point_count"),
                                    color(android.graphics.Color.parseColor("#3B82F6")), // Blue for < 20 points
                                    literal(20), color(android.graphics.Color.parseColor("#F59E0B")), // Gold for 20-100 points
                                    literal(100), color(android.graphics.Color.parseColor("#EF4444"))  // Red for > 100 points
                                )
                            ),
                            circleRadius(
                                step(
                                    get("point_count"),
                                    literal(18f), // Radius for < 20 points
                                    literal(20), literal(24f), // Radius for 20-100 points
                                    literal(100), literal(30f)  // Radius for > 100 points
                                )
                            ),
                            circleStrokeWidth(2f),
                            circleStrokeColor("#FFFFFF"),
                            circleOpacity(0.85f)
                        )
                    }
                    style.addLayer(clustersLayer)

                    // 3. Add Cluster Count Labels
                    val clusterCountLayer = SymbolLayer(LAYER_CLUSTER_COUNT, ATLAS_GEOJSON_SOURCE).apply {
                        setFilter(has("point_count"))
                        setProperties(
                            textField(toString(get("point_count"))),
                            textSize(12f),
                            textColor("#FFFFFF"),
                            textIgnorePlacement(true),
                            textAllowOverlap(true)
                        )
                    }
                    style.addLayer(clusterCountLayer)

                    // 4. Add Individual (Unclustered) Points Layer
                    val unclusteredLayer = CircleLayer(LAYER_UNCLUSTERED_POINTS, ATLAS_GEOJSON_SOURCE).apply {
                        setFilter(not(has("point_count")))
                        setProperties(
                            circleColor(
                                match(
                                    get("layerType"),
                                    literal(AtlasLayerType.BOTANY.name), color(android.graphics.Color.parseColor("#10B981")),
                                    literal(AtlasLayerType.HISTORY.name), color(android.graphics.Color.parseColor("#F59E0B")),
                                    color(android.graphics.Color.parseColor("#3B82F6")) // Science default
                                )
                            ),
                            circleRadius(7f),
                            circleStrokeWidth(2f),
                            circleStrokeColor("#FFFFFF"),
                            circleOpacity(0.9f)
                        )
                    }
                    style.addLayer(unclusteredLayer)

                    // 5. Handle Map Tap Events (Cluster Zoom & Single POI Selection)
                    map.addOnMapClickListener { latLng ->
                        val screenPoint = map.projection.toScreenLocation(latLng)
                        
                        // Check if user clicked a cluster circle
                        val clusterFeatures = map.queryRenderedFeatures(screenPoint, LAYER_CLUSTERS)
                        if (clusterFeatures.isNotEmpty()) {
                            val source = style.getSourceAs<GeoJsonSource>(ATLAS_GEOJSON_SOURCE)
                            val cluster = clusterFeatures[0]
                            
                            // Native zoom-to-cluster expansion
                            val zoom = (source?.getClusterExpansionZoom(cluster)?.toDouble() ?: (map.cameraPosition.zoom + 2.0))
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(latLng, zoom)
                            )
                            return@addOnMapClickListener true
                        }

                        // Check if user clicked an unclustered POI node
                        val poiFeatures = map.queryRenderedFeatures(screenPoint, LAYER_UNCLUSTERED_POINTS)
                        if (poiFeatures.isNotEmpty()) {
                            val clickedId = poiFeatures[0].getStringProperty("id")
                            pointsOfInterest.find { it.id == clickedId }?.let { selected ->
                                onPoiSelected(selected)
                            }
                            return@addOnMapClickListener true
                        }

                        false
                    }
                }
            } else {
                updateClusteredData(map, pointsOfInterest, activeLayers)
            }
        }
    }
}

/**
 * Filters points by active UI layers and updates GeoJSON source
 */
private fun updateClusteredData(
    map: MapLibreMap,
    points: List<AtlasPointOfInterest>,
    activeLayers: Set<AtlasLayerType>
) {
    val style = map.style ?: return
    val source = style.getSourceAs<GeoJsonSource>(ATLAS_GEOJSON_SOURCE) ?: return

    val filteredPoints = points.filter { activeLayers.contains(it.layerType) }

    val featureCollection = org.maplibre.geojson.FeatureCollection.fromFeatures(
        filteredPoints.map { poi ->
            val feature = org.maplibre.geojson.Feature.fromGeometry(
                org.maplibre.geojson.Point.fromLngLat(poi.longitude, poi.latitude)
            )
            feature.addStringProperty("id", poi.id)
            feature.addStringProperty("title", poi.title)
            feature.addStringProperty("layerType", poi.layerType.name)
            feature
        }
    )
    source.setGeoJson(featureCollection)
}
