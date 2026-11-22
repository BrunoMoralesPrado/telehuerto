package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
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
import com.example.telehuerto.data.LogEntry
import com.example.telehuerto.data.LogType
import com.example.telehuerto.data.Plant
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PlantPotIllustration
import com.example.telehuerto.ui.theme.*

private val SegmentedBarBg = Color(0xFFE5EAE0)
private val TimelineLineColor = Color(0xFFDCE2D5)

private val FotoBg = Color(0xFFDFEDE2)
private val AutomaticoTint = Color(0xFF3B7A8C)
private val AutomaticoBg = Color(0xFFDFEFF2)
private val NotaTint = Color(0xFFB58938)
private val NotaBg = Color(0xFFF9EDD4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogbookScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedPlantFilterId by remember { mutableStateOf<String?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<LogType?>(null) }
    var showTypeFilterRow by remember { mutableStateOf(false) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var preselectedDialogPlantId by remember { mutableStateOf<String?>(null) }

    val allEntries = HuertoRepository.logEntries
    val activePlants = HuertoRepository.activePlants()
    val allPlants = HuertoRepository.plants
    val wifiSyncPercent = HuertoRepository.arduinoDevice.wifiSignalPercent

    val chronologicalEntries = if (selectedTypeFilter == null) {
        allEntries
    } else {
        allEntries.filter { it.type == selectedTypeFilter }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, "logbook") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    preselectedDialogPlantId = selectedPlantFilterId ?: activePlants.firstOrNull()?.id
                    showAddEntryDialog = true
                },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Registrar avance",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = BackgroundBeige
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HISTORIAL",
                        fontSize = 11.sp,
                        letterSpacing = 1.4.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Bitácora",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Surface(
                    onClick = { showTypeFilterRow = !showTypeFilterRow },
                    shape = RoundedCornerShape(14.dp),
                    color = if (showTypeFilterRow || selectedTypeFilter != null) FotoBg else CardBackground,
                    modifier = Modifier.size(44.dp),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtrar eventos",
                            tint = GreenPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    value = "${allEntries.size + 8}",
                    label = "Eventos este\nmes",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${activePlants.size}",
                    label = "Plantas\nactivas",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${wifiSyncPercent}%",
                    label = "Datos\nsincronizados",
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = SegmentedBarBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        text = "Cronológica",
                        selected = selectedTab == 0,
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = 0 }
                    TabButton(
                        text = "Por planta",
                        selected = selectedTab == 1,
                        modifier = Modifier.weight(1f)
                    ) { selectedTab = 1 }
                }
            }

            if (showTypeFilterRow && selectedTab == 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogFilterChip(
                        label = "Todos",
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null }
                    )
                    LogType.entries.forEach { logType ->
                        LogFilterChip(
                            label = logType.label,
                            selected = selectedTypeFilter == logType,
                            onClick = {
                                selectedTypeFilter = if (selectedTypeFilter == logType) null else logType
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    itemsIndexed(chronologicalEntries, key = { _, item -> item.id }) { index, entry ->
                        LogTimelineItem(
                            entry = entry,
                            isLast = index == chronologicalEntries.lastIndex,
                            onClick = {
                                entry.plantId?.let { pid ->
                                    HuertoRepository.selectPlant(pid)
                                    navController.navigate("plant_detail")
                                }
                            }
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogFilterChip(
                            label = "Todas (${allPlants.size})",
                            selected = selectedPlantFilterId == null,
                            onClick = { selectedPlantFilterId = null }
                        )
                        allPlants.forEach { plant ->
                            LogFilterChip(
                                label = plant.name,
                                selected = selectedPlantFilterId == plant.id,
                                onClick = {
                                    selectedPlantFilterId = if (selectedPlantFilterId == plant.id) null else plant.id
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val displayedPlants = if (selectedPlantFilterId == null) {
                        allPlants
                    } else {
                        allPlants.filter { it.id == selectedPlantFilterId }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(displayedPlants, key = { it.id }) { plant ->
                            val plantEntries = allEntries.filter {
                                it.plantId == plant.id ||
                                    it.plantName?.equals(plant.name, ignoreCase = true) == true ||
                                    it.title.contains(plant.name, ignoreCase = true) ||
                                    it.description.contains(plant.name, ignoreCase = true)
                            }
                            PlantEvolutionSection(
                                plant = plant,
                                entries = plantEntries,
                                onViewPlant = {
                                    HuertoRepository.selectPlant(plant.id)
                                    navController.navigate("plant_detail")
                                },
                                onAddPlantEntry = {
                                    preselectedDialogPlantId = plant.id
                                    showAddEntryDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddEntryDialog) {
        AddGrowthEntryDialog(
            plants = activePlants.ifEmpty { allPlants },
            initialPlantId = preselectedDialogPlantId,
            onDismiss = { showAddEntryDialog = false },
            onConfirm = { type, title, description, plant ->
                HuertoRepository.addLogEntry(
                    type = type,
                    title = title,
                    description = description,
                    plantId = plant?.id,
                    plantName = plant?.name
                )
                showAddEntryDialog = false
            }
        )
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTimelineItem(
    entry: LogEntry,
    isLast: Boolean = false,
    onClick: () -> Unit = {}
) {
    val (icon, iconTint, iconBg) = logTypeVisuals(entry.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = entry.type.label,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .background(TimelineLineColor)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Card(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.timeText,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = entry.type.label,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (!entry.plantName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = entry.plantName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.description,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantEvolutionSection(
    plant: Plant,
    entries: List<LogEntry>,
    onViewPlant: () -> Unit,
    onAddPlantEntry: () -> Unit
) {
    val statusBg = when (plant.status) {
        PlantStatus.SALUDABLE -> FotoBg
        PlantStatus.REQUIERE_ATENCION -> Color(0xFFF9E3DC)
        PlantStatus.DADA_DE_BAJA -> Color(0xFFE6E8E4)
    }
    val statusFg = when (plant.status) {
        PlantStatus.SALUDABLE -> GreenPrimary
        PlantStatus.REQUIERE_ATENCION -> Color(0xFFC94A38)
        PlantStatus.DADA_DE_BAJA -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewPlant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlantPotIllustration(
                    iconType = plant.iconType,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plant.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = statusBg
                        ) {
                            Text(
                                text = plant.status.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusFg,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "${plant.species} · Sembrada ${plant.plantedDate}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${plant.arduinoId} · ${plant.sensorChannel} · ${entries.size} registros",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenPrimary
                    )
                }
            }

            if (plant.retiredReason != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF3F4F1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Motivo de baja: ${plant.retiredReason}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BackgroundBeige)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evolución y eventos registrados",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(
                    onClick = onAddPlantEntry,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+ Añadir nota/foto",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "Sin eventos registrados todavía para esta planta.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                entries.forEach { entry ->
                    val (icon, tint, bg) = logTypeVisuals(entry.type)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = entry.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = entry.timeText,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = entry.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) GreenPrimary else CardBackground,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFD9DFD3))
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGrowthEntryDialog(
    plants: List<Plant>,
    initialPlantId: String?,
    onDismiss: () -> Unit,
    onConfirm: (LogType, String, String, Plant?) -> Unit
) {
    var selectedType by remember { mutableStateOf(LogType.FOTO) }
    var selectedPlant by remember {
        mutableStateOf(plants.find { it.id == initialPlantId } ?: plants.firstOrNull())
    }
    var title by remember { mutableStateOf("Nuevos brotes en tallo principal") }
    var description by remember {
        mutableStateOf("Fotografía de seguimiento registrada. Las hojas muestran color verde intenso y crecimiento saludable.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "BITÁCORA DE EVOLUCIÓN",
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Registrar avance o foto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tipo de registro",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = {
                            selectedType = LogType.FOTO
                            if (title.isBlank()) title = "Crecimiento de ${selectedPlant?.name ?: "planta"}"
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == LogType.FOTO) FotoBg else BackgroundBeige,
                        border = if (selectedType == LogType.FOTO) BorderStroke(1.5.dp, GreenPrimary) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Fotografía",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            selectedType = LogType.NOTA
                            if (title == "Nuevos brotes en tallo principal") {
                                title = "Poda y abono orgánico"
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == LogType.NOTA) NotaBg else BackgroundBeige,
                        border = if (selectedType == LogType.NOTA) BorderStroke(1.5.dp, NotaTint) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = NotaTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nota cultivo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NotaTint
                            )
                        }
                    }
                }

                Text(
                    text = "Planta asociada",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    plants.forEach { plant ->
                        val isSelected = selectedPlant?.id == plant.id
                        Surface(
                            onClick = { selectedPlant = plant },
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) GreenPrimary else BackgroundBeige
                        ) {
                            Text(
                                text = plant.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del evento") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Observaciones de evolución") },
                    minLines = 3,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.trim().ifEmpty {
                        "Avance de ${selectedPlant?.name ?: "huerto"}"
                    }
                    val finalDesc = description.trim().ifEmpty {
                        "Registro manual añadido a la bitácora de crecimiento."
                    }
                    onConfirm(selectedType, finalTitle, finalDesc, selectedPlant)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar en bitácora", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

private fun logTypeVisuals(type: LogType): Triple<ImageVector, Color, Color> = when (type) {
    LogType.FOTO -> Triple(Icons.Default.PhotoCamera, GreenPrimary, FotoBg)
    LogType.AUTOMATICO -> Triple(Icons.Default.WaterDrop, AutomaticoTint, AutomaticoBg)
    LogType.NOTA -> Triple(Icons.Default.MenuBook, NotaTint, NotaBg)
    LogType.ALERTA -> Triple(Icons.Default.Check, GreenPrimary, FotoBg)
}
