package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.Plant
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.data.SyncStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PlantAvatar
import com.example.telehuerto.ui.theme.AlertCritical
import com.example.telehuerto.ui.theme.AlertCriticalBg
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.BorderSubtle
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenDark
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.GreenSoft
import com.example.telehuerto.ui.theme.HumidityBlue
import com.example.telehuerto.ui.theme.NutrientGreen
import com.example.telehuerto.ui.theme.StatusAttention
import com.example.telehuerto.ui.theme.StatusAttentionBg
import com.example.telehuerto.ui.theme.StatusHealthy
import com.example.telehuerto.ui.theme.StatusHealthyBg
import com.example.telehuerto.ui.theme.StatusRetired
import com.example.telehuerto.ui.theme.StatusRetiredBg
import com.example.telehuerto.ui.theme.SunYellow
import com.example.telehuerto.ui.theme.SurfaceTinted
import com.example.telehuerto.ui.theme.SyncPending
import com.example.telehuerto.ui.theme.SyncPendingBg
import com.example.telehuerto.ui.theme.TextOnDark
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary

@Composable
fun PlantsListScreen(navController: NavController) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showFilters by rememberSaveable { mutableStateOf(true) }
    var selectedFilter by rememberSaveable { mutableIntStateOf(0) }

    val allPlants = HuertoRepository.plants
    val activePlants = HuertoRepository.activePlants()
    val retiredPlants = HuertoRepository.retiredPlants()
    val attentionPlants = activePlants.filter { it.status == PlantStatus.REQUIERE_ATENCION }

    val baseList = when (selectedFilter) {
        1 -> attentionPlants
        2 -> retiredPlants
        else -> activePlants
    }
    val query = searchQuery.trim()
    val filtered = if (query.isEmpty()) {
        baseList
    } else {
        baseList.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.species.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
        }
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
            // Encabezado
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COLECCIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.4.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Mis plantas",
                            style = MaterialTheme.typography.displaySmall,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (showFilters) GreenSoft else CardBackground,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (showFilters) GreenPrimary.copy(alpha = 0.25f) else BorderSubtle
                        )
                    ) {
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = if (showFilters) "Ocultar filtros" else "Mostrar filtros",
                                tint = if (showFilters) GreenDark else TextPrimary
                            )
                        }
                    }
                }
            }

            // Buscador
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Buscar por nombre o especie",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            // Chips de filtro
            if (showFilters) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipPill(
                            label = "Activas (${activePlants.size})",
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 }
                        )
                        FilterChipPill(
                            label = "Requieren atención (${attentionPlants.size})",
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 }
                        )
                        FilterChipPill(
                            label = "Dadas de baja (${retiredPlants.size})",
                            selected = selectedFilter == 2,
                            onClick = { selectedFilter = 2 }
                        )
                    }
                }
            }

            // Contenido o estado vacío
            if (filtered.isEmpty()) {
                item {
                    EmptyPlantsCard(
                        hasAnyPlants = allPlants.isNotEmpty(),
                        query = query,
                        selectedFilter = selectedFilter,
                        onClearSearch = { searchQuery = "" },
                        onAddPlant = { navController.navigate("add_plant") }
                    )
                }
            } else {
                items(filtered, key = { it.id }) { plant ->
                    PlantCard(
                        plant = plant,
                        onClick = {
                            HuertoRepository.selectPlant(plant.id)
                            navController.navigate("plant_detail/${plant.id}")
                        }
                    )
                }
            }

            // Botón principal inferior
            if (allPlants.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Button(
                        onClick = { navController.navigate("add_plant") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = TextOnDark
                        )
                    ) {
                        Text(
                            text = "+ Añadir nueva planta",
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) GreenPrimary else CardBackground,
        border = if (selected) null else BorderStroke(1.dp, BorderSubtle)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) TextOnDark else TextPrimary,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlantCard(
    plant: Plant,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlantAvatar(
                iconType = plant.iconType,
                photoUri = plant.photoUri,
                modifier = Modifier.size(80.dp),
                cornerRadius = 16.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = plant.species,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PlantStatusPill(status = plant.status)
                    PlantSyncBadge(syncStatus = plant.syncStatus)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SensorMiniMetric(
                        icon = Icons.Outlined.WaterDrop,
                        label = "H ${plant.humidity}%",
                        tint = HumidityBlue
                    )
                    SensorMiniMetric(
                        icon = Icons.Outlined.WbSunny,
                        label = "Luz ${plant.sunPercentage}%",
                        tint = SunYellow
                    )
                    SensorMiniMetric(
                        icon = Icons.Default.Equalizer,
                        label = "NPK ${plant.nutrientsPercentage}%",
                        tint = NutrientGreen
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PlantStatusPill(status: PlantStatus) {
    val (bg, fg) = when (status) {
        PlantStatus.SALUDABLE -> StatusHealthyBg to StatusHealthy
        PlantStatus.REQUIERE_ATENCION -> StatusAttentionBg to StatusAttention
        PlantStatus.DADA_DE_BAJA -> StatusRetiredBg to StatusRetired
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg)
        )
        Text(
            text = status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
private fun PlantSyncBadge(syncStatus: SyncStatus) {
    val (bg, fg, icon) = when (syncStatus) {
        SyncStatus.SYNCED -> Triple(StatusHealthyBg.copy(alpha = 0.7f), StatusHealthy, Icons.Outlined.CloudDone)
        SyncStatus.PENDING -> Triple(SyncPendingBg, SyncPending, Icons.Outlined.CloudUpload)
        SyncStatus.ERROR -> Triple(AlertCriticalBg, AlertCritical, Icons.Outlined.CloudOff)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = syncStatus.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
private fun SensorMiniMetric(
    icon: ImageVector,
    label: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun EmptyPlantsCard(
    hasAnyPlants: Boolean,
    query: String,
    selectedFilter: Int,
    onClearSearch: () -> Unit,
    onAddPlant: () -> Unit
) {
    val icon = when {
        query.isNotEmpty() -> Icons.Outlined.SearchOff
        !hasAnyPlants -> Icons.Outlined.Eco
        selectedFilter == 1 -> Icons.Outlined.CheckCircle
        else -> Icons.Outlined.Eco
    }
    val title = when {
        query.isNotEmpty() -> "Sin resultados para \"$query\""
        !hasAnyPlants -> "Tu huerto aún está vacío"
        selectedFilter == 1 -> "Todo en orden por aquí"
        selectedFilter == 2 -> "No hay plantas dadas de baja"
        else -> "No hay plantas activas"
    }
    val subtitle = when {
        query.isNotEmpty() -> "Prueba con otro nombre, especie o cambia el filtro."
        !hasAnyPlants -> "Registra tu primera planta para ver sus lecturas de humedad, luz y nutrientes."
        selectedFilter == 1 -> "Ninguna de tus plantas activas necesita atención en este momento."
        selectedFilter == 2 -> "Las plantas que retires del huerto aparecerán aquí por si quieres restaurarlas."
        else -> "Todas tus plantas están dadas de baja o aún no has agregado una nueva."
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceTinted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            if (query.isNotEmpty()) {
                TextButton(onClick = onClearSearch) {
                    Text(
                        text = "Limpiar búsqueda",
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (!hasAnyPlants) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAddPlant,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = TextOnDark
                    )
                ) {
                    Text(
                        text = "Añadir nueva planta",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
