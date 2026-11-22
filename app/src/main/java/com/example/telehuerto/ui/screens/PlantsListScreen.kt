package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.Plant
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PlantPotIllustration
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary

private val PillHealthyBg = Color(0xFFDFEDE2)
private val PillAttentionBg = Color(0xFFF9E3DC)
private val PillAttentionFg = Color(0xFFC94A38)
private val PillRetiredBg = Color(0xFFE6E8E4)
private val WaterColor = Color(0xFF3B7A8C)
private val SunColor = Color(0xFFB58938)
private val NutrientColor = Color(0xFFB55B52)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsListScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var filterIndex by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(true) }

    val activePlants = HuertoRepository.activePlants()
    val retiredPlants = HuertoRepository.retiredPlants()
    val attentionPlants = activePlants.filter { it.status == PlantStatus.REQUIERE_ATENCION }

    val base = when (filterIndex) {
        1 -> attentionPlants
        2 -> retiredPlants
        else -> activePlants
    }
    val query = searchQuery.trim()
    val filtered = if (query.isEmpty()) base else base.filter {
        it.name.contains(query, ignoreCase = true) || it.species.contains(query, ignoreCase = true)
    }

    Scaffold(
        containerColor = BackgroundBeige,
        bottomBar = { BottomNavBar(navController, "plants") }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "COLECCIÓN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = TextSecondary
                        )
                        Text(
                            "Mis plantas",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (showFilters) PillHealthyBg else CardBackground, RoundedCornerShape(14.dp))
                            .clickable { showFilters = !showFilters },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Filtrar", tint = GreenPrimary)
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar una planta", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
            if (showFilters) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipItem("Activas (${activePlants.size})", filterIndex == 0) { filterIndex = 0 }
                        FilterChipItem("Requieren atención", filterIndex == 1) { filterIndex = 1 }
                        FilterChipItem("Dadas de baja (${retiredPlants.size})", filterIndex == 2) { filterIndex = 2 }
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = when {
                            query.isNotEmpty() -> "No encontramos plantas que coincidan con \"$query\"."
                            filterIndex == 1 -> "Ninguna planta necesita atención ahora."
                            filterIndex == 2 -> "Todavía no has dado de baja ninguna planta."
                            else -> "Aún no tienes plantas. Añade la primera."
                        },
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(filtered, key = { it.id }) { plant ->
                    PlantCard(plant) {
                        HuertoRepository.selectPlant(plant.id)
                        navController.navigate("plant_detail")
                    }
                }
            }
            item {
                Button(
                    onClick = { navController.navigate("add_plant") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
                ) {
                    Text("+ Añadir nueva planta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) GreenPrimary else CardBackground,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFD9DFD3))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextPrimary,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantPotIllustration(iconType = plant.iconType, modifier = Modifier.size(80.dp))
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
                Text(plant.species, fontSize = 13.sp, color = TextSecondary, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                StatusPill(plant.status)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    MiniMetric(Icons.Default.WaterDrop, "${plant.humidity}%", WaterColor)
                    MiniMetric(Icons.Default.WbSunny, "${plant.sunPercentage}%", SunColor)
                    MiniMetric(Icons.Default.Equalizer, "${plant.nutrientsPercentage}%", NutrientColor)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun StatusPill(status: PlantStatus) {
    val (bg, fg) = when (status) {
        PlantStatus.SALUDABLE -> PillHealthyBg to GreenPrimary
        PlantStatus.REQUIERE_ATENCION -> PillAttentionBg to PillAttentionFg
        PlantStatus.DADA_DE_BAJA -> PillRetiredBg to TextSecondary
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text("● ${status.label}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun MiniMetric(icon: ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
