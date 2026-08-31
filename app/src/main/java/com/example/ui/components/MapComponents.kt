package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtlasLayerType
import com.example.data.model.AtlasPointOfInterest
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.NeonBotany
import com.example.ui.theme.NeonHistory
import com.example.ui.theme.NeonScience
import com.example.ui.theme.SurfaceBorder

@Composable
fun FloatingLayerBar(
    activeLayers: Set<AtlasLayerType>,
    onLayerToggle: (AtlasLayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(GlassBackground),
        color = GlassBackground,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LayerChip(
                label = "🌿 Botany",
                accentColor = NeonBotany,
                isSelected = activeLayers.contains(AtlasLayerType.BOTANY),
                onClick = { onLayerToggle(AtlasLayerType.BOTANY) }
            )
            LayerChip(
                label = "🏛️ History",
                accentColor = NeonHistory,
                isSelected = activeLayers.contains(AtlasLayerType.HISTORY),
                onClick = { onLayerToggle(AtlasLayerType.HISTORY) }
            )
            LayerChip(
                label = "🔬 Science",
                accentColor = NeonScience,
                isSelected = activeLayers.contains(AtlasLayerType.SCIENCE),
                onClick = { onLayerToggle(AtlasLayerType.SCIENCE) }
            )
        }
    }
}

@Composable
fun LayerChip(
    label: String,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent
    val border = if (isSelected) accentColor else Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else Color.Gray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PoiDetailPanel(
    poi: AtlasPointOfInterest,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = GlassBackground,
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poi.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Text("✕", color = Color.Gray, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Coordinates: ${poi.latitude}, ${poi.longitude}",
                color = Color.LightGray,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Launch Wikipedia Web View */ },
                colors = ButtonDefaults.buttonColors(containerColor = NeonScience),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Encyclopedia Entry", color = Color.White)
            }
        }
    }
}
