package com.example.telehuerto.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.LogEntry
import com.example.telehuerto.data.LogType
import com.example.telehuerto.data.Plant
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.data.SyncStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PhotoStorageHelper
import com.example.telehuerto.ui.components.PhotoThumbnail
import com.example.telehuerto.ui.components.PlantAvatar
import com.example.telehuerto.ui.theme.AlertCritical
import com.example.telehuerto.ui.theme.AlertCriticalBg
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.BorderSubtle
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.GreenSoft
import com.example.telehuerto.ui.theme.StatusAttention
import com.example.telehuerto.ui.theme.StatusAttentionBg
import com.example.telehuerto.ui.theme.StatusHealthy
import com.example.telehuerto.ui.theme.StatusHealthyBg
import com.example.telehuerto.ui.theme.StatusRetired
import com.example.telehuerto.ui.theme.StatusRetiredBg
import com.example.telehuerto.ui.theme.SurfaceTinted
import com.example.telehuerto.ui.theme.SyncPending
import com.example.telehuerto.ui.theme.SyncPendingBg
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import com.example.telehuerto.util.Validators
import kotlinx.coroutines.launch
import java.io.File

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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedPlantFilterId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTypeFilterName by rememberSaveable { mutableStateOf<String?>(null) }
    var showTypeFilterRow by rememberSaveable { mutableStateOf(false) }
    var showEntryDialog by rememberSaveable { mutableStateOf(false) }
    var editingEntryId by rememberSaveable { mutableStateOf<String?>(null) }
    var entryIdPendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var preselectedDialogPlantId by rememberSaveable { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val allEntries = HuertoRepository.logEntries
    val activePlants = HuertoRepository.activePlants()
    val allPlants = HuertoRepository.plants
    val syncedPercent = HuertoRepository.syncedPercentage()

    val selectedTypeFilter = selectedTypeFilterName?.let { name ->
        LogType.entries.firstOrNull { it.name == name }
    }

    val chronologicalEntries = if (selectedTypeFilter == null) {
        allEntries
    } else {
        allEntries.filter { it.type == selectedTypeFilter }
    }

    val editingEntry = editingEntryId?.let { id ->
        allEntries.firstOrNull { it.id == id }
    }

    fun navigateToPlantIfAvailable(plantId: String?) {
        if (plantId.isNullOrBlank()) return
        if (HuertoRepository.getPlantById(plantId) != null) {
            HuertoRepository.selectPlant(plantId)
            navController.navigate("plant_detail/$plantId")
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Esta planta ya fue eliminada del huerto.")
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, "logbook") },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingEntryId = null
                    preselectedDialogPlantId = selectedPlantFilterId ?: activePlants.firstOrNull()?.id
                    showEntryDialog = true
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
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.size(44.dp)
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
                    value = "${HuertoRepository.logEntries.size}",
                    label = "Eventos\nregistrados",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${HuertoRepository.activePlants().size}",
                    label = "Plantas\nactivas",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${syncedPercent}%",
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
                        onClick = { selectedTypeFilterName = null }
                    )
                    LogType.entries.forEach { logType ->
                        LogFilterChip(
                            label = logType.label,
                            selected = selectedTypeFilter == logType,
                            onClick = {
                                selectedTypeFilterName =
                                    if (selectedTypeFilter == logType) null else logType.name
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                if (chronologicalEntries.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sin registros en la bitácora",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Registra una fotografía o nota de evolución para comenzar el seguimiento.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        itemsIndexed(chronologicalEntries, key = { _, item -> item.id }) { index, entry ->
                            LogTimelineItem(
                                entry = entry,
                                isLast = index == chronologicalEntries.lastIndex,
                                onClick = { navigateToPlantIfAvailable(entry.plantId) },
                                onEdit = {
                                    editingEntryId = entry.id
                                    preselectedDialogPlantId = entry.plantId
                                    showEntryDialog = true
                                },
                                onDelete = {
                                    entryIdPendingDelete = entry.id
                                }
                            )
                        }
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
                                    selectedPlantFilterId =
                                        if (selectedPlantFilterId == plant.id) null else plant.id
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

                    if (displayedPlants.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CardBackground,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No hay plantas registradas",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Agrega una planta al huerto para consultar su evolución.",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(displayedPlants, key = { it.id }) { plant ->
                                val plantEntries = allEntries.filter {
                                    it.plantId == plant.id ||
                                        it.plantName?.equals(plant.name, ignoreCase = true) == true
                                }
                                PlantEvolutionSection(
                                    plant = plant,
                                    entries = plantEntries,
                                    onViewPlant = { navigateToPlantIfAvailable(plant.id) },
                                    onAddPlantEntry = {
                                        editingEntryId = null
                                        preselectedDialogPlantId = plant.id
                                        showEntryDialog = true
                                    },
                                    onEditEntry = { entry ->
                                        editingEntryId = entry.id
                                        preselectedDialogPlantId = entry.plantId ?: plant.id
                                        showEntryDialog = true
                                    },
                                    onDeleteEntry = { entry ->
                                        entryIdPendingDelete = entry.id
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEntryDialog) {
        AddOrEditGrowthEntryDialog(
            plants = activePlants.ifEmpty { allPlants },
            initialPlantId = preselectedDialogPlantId,
            editingEntry = editingEntry,
            onDismiss = {
                showEntryDialog = false
                editingEntryId = null
            },
            onConfirm = { type, title, description, plant, photoUri ->
                val targetEdit = editingEntry
                if (targetEdit == null) {
                    HuertoRepository.addLogEntry(
                        type = type,
                        title = title,
                        description = description,
                        plantId = plant?.id,
                        plantName = plant?.name,
                        photoUri = photoUri
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Registro guardado en la bitácora.")
                    }
                } else {
                    HuertoRepository.updateLogEntry(
                        entryId = targetEdit.id,
                        type = type,
                        title = title,
                        description = description,
                        plantId = plant?.id,
                        plantName = plant?.name,
                        photoUri = photoUri
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Registro actualizado.")
                    }
                }
                showEntryDialog = false
                editingEntryId = null
            }
        )
    }

    val pendingDeleteEntry = entryIdPendingDelete?.let { id ->
        allEntries.firstOrNull { it.id == id }
    }
    if (pendingDeleteEntry != null) {
        AlertDialog(
            onDismissRequest = { entryIdPendingDelete = null },
            containerColor = CardBackground,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Eliminar registro de bitácora",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Se eliminará permanentemente \"${pendingDeleteEntry.title}\" de la bitácora.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToRemove = pendingDeleteEntry.id
                        entryIdPendingDelete = null
                        HuertoRepository.deleteLogEntry(idToRemove)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Registro eliminado de la bitácora.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertCritical,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryIdPendingDelete = null }) {
                    Text(text = "Cancelar", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
    onClick: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
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
            border = BorderStroke(1.dp, BorderSubtle),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = entry.timeText,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "·",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = entry.type.label,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconTint
                        )
                        if (entry.syncStatus != SyncStatus.SYNCED) {
                            SyncStatusPill(status = entry.syncStatus)
                        }
                    }

                    if (onEdit != null || onDelete != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (onEdit != null) {
                                IconButton(
                                    onClick = onEdit,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Editar registro",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (onDelete != null) {
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Eliminar registro",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
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
                if (!entry.photoUri.isNullOrBlank()) {
                    PhotoThumbnail(
                        photoUri = entry.photoUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusPill(status: SyncStatus) {
    val bg = when (status) {
        SyncStatus.PENDING -> SyncPendingBg
        SyncStatus.ERROR -> AlertCriticalBg
        SyncStatus.SYNCED -> GreenSoft
    }
    val fg = when (status) {
        SyncStatus.PENDING -> SyncPending
        SyncStatus.ERROR -> AlertCritical
        SyncStatus.SYNCED -> GreenPrimary
    }
    val icon = when (status) {
        SyncStatus.PENDING -> Icons.Outlined.CloudUpload
        SyncStatus.ERROR -> Icons.Outlined.CloudOff
        SyncStatus.SYNCED -> Icons.Default.Check
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = status.label,
                tint = fg,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = status.label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantEvolutionSection(
    plant: Plant,
    entries: List<LogEntry>,
    onViewPlant: () -> Unit,
    onAddPlantEntry: () -> Unit,
    onEditEntry: (LogEntry) -> Unit,
    onDeleteEntry: (LogEntry) -> Unit
) {
    val statusBg = when (plant.status) {
        PlantStatus.SALUDABLE -> StatusHealthyBg
        PlantStatus.REQUIERE_ATENCION -> StatusAttentionBg
        PlantStatus.DADA_DE_BAJA -> StatusRetiredBg
    }
    val statusFg = when (plant.status) {
        PlantStatus.SALUDABLE -> StatusHealthy
        PlantStatus.REQUIERE_ATENCION -> StatusAttention
        PlantStatus.DADA_DE_BAJA -> StatusRetired
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewPlant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlantAvatar(
                    iconType = plant.iconType,
                    photoUri = plant.photoUri,
                    modifier = Modifier.size(48.dp),
                    cornerRadius = 12.dp
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
                    color = SurfaceTinted,
                    border = BorderStroke(1.dp, BorderSubtle),
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
                    .background(BorderSubtle)
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onEditEntry(entry) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Editar",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteEntry(entry) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Eliminar",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = entry.description,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                            if (!entry.photoUri.isNullOrBlank()) {
                                PhotoThumbnail(
                                    photoUri = entry.photoUri,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(top = 8.dp)
                                )
                            }
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
        border = if (selected) null else BorderStroke(1.dp, BorderSubtle)
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
fun AddOrEditGrowthEntryDialog(
    plants: List<Plant>,
    initialPlantId: String?,
    editingEntry: LogEntry?,
    onDismiss: () -> Unit,
    onConfirm: (LogType, String, String, Plant?, String?) -> Unit
) {
    val context = LocalContext.current
    val dialogKey = editingEntry?.id ?: "new"

    var selectedTypeName by rememberSaveable(dialogKey) {
        mutableStateOf((editingEntry?.type ?: LogType.FOTO).name)
    }
    var selectedPlantId by rememberSaveable(dialogKey) {
        mutableStateOf(
            editingEntry?.plantId
                ?: initialPlantId
                ?: plants.firstOrNull()?.id
                ?: ""
        )
    }
    var title by rememberSaveable(dialogKey) {
        mutableStateOf(editingEntry?.title ?: "")
    }
    var description by rememberSaveable(dialogKey) {
        mutableStateOf(editingEntry?.description ?: "")
    }
    var photoUri by rememberSaveable(dialogKey) {
        mutableStateOf(editingEntry?.photoUri)
    }
    var pendingCameraFilePath by rememberSaveable(dialogKey) {
        mutableStateOf<String?>(null)
    }
    var titleError by rememberSaveable(dialogKey) {
        mutableStateOf<String?>(null)
    }
    var descriptionError by rememberSaveable(dialogKey) {
        mutableStateOf<String?>(null)
    }

    val selectedType = LogType.entries.firstOrNull { it.name == selectedTypeName } ?: LogType.FOTO
    val selectedPlant = plants.firstOrNull { it.id == selectedPlantId } ?: plants.firstOrNull()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = PhotoStorageHelper.copyUriToInternalStorage(context, uri)
            if (!savedPath.isNullOrBlank()) {
                photoUri = savedPath
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val path = pendingCameraFilePath
        if (success && !path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists() && file.length() > 0L) {
                photoUri = file.absolutePath
            }
        } else if (!path.isNullOrBlank()) {
            PhotoStorageHelper.deletePhotoIfManaged(path)
        }
        pendingCameraFilePath = null
    }

    fun launchCameraCapture() {
        val (targetUri, targetFile) = PhotoStorageHelper.createCameraPhotoTarget(context)
        pendingCameraFilePath = targetFile.absolutePath
        cameraLauncher.launch(targetUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        }
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
                    text = if (editingEntry == null) "Registrar avance o foto" else "Editar registro de bitácora",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        onClick = { selectedTypeName = LogType.FOTO.name },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == LogType.FOTO) FotoBg else BackgroundBeige,
                        border = if (selectedType == LogType.FOTO) BorderStroke(1.5.dp, GreenPrimary) else BorderStroke(1.dp, BorderSubtle),
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
                        onClick = { selectedTypeName = LogType.NOTA.name },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedType == LogType.NOTA) NotaBg else BackgroundBeige,
                        border = if (selectedType == LogType.NOTA) BorderStroke(1.5.dp, NotaTint) else BorderStroke(1.dp, BorderSubtle),
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

                if (plants.isNotEmpty()) {
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
                                onClick = { selectedPlantId = plant.id },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) GreenPrimary else BackgroundBeige,
                                border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle)
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
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (titleError != null) {
                                val check = Validators.validateLogTitle(it)
                                if (check.isValid) titleError = null
                            }
                        },
                        label = { Text("Título del evento") },
                        placeholder = { Text("Ej. Nuevos brotes en tallo principal") },
                        isError = titleError != null,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (titleError != null) {
                        Text(
                            text = titleError!!,
                            color = AlertCritical,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            if (descriptionError != null) {
                                val check = Validators.validateLogDescription(it)
                                if (check.isValid) descriptionError = null
                            }
                        },
                        label = { Text("Observaciones de evolución") },
                        placeholder = { Text("Describe el crecimiento, riego, poda o estado de las hojas...") },
                        isError = descriptionError != null,
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (descriptionError != null) {
                        Text(
                            text = descriptionError!!,
                            color = AlertCritical,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Evidencia fotográfica (opcional)",
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
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceTinted,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Galería",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                launchCameraCapture()
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceTinted,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cámara",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                if (!photoUri.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PhotoThumbnail(
                            photoUri = photoUri,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                        Surface(
                            onClick = { photoUri = null },
                            shape = CircleShape,
                            color = CardBackground.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Quitar foto",
                                    tint = AlertCritical,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val titleValidation = Validators.validateLogTitle(title)
                    val descValidation = Validators.validateLogDescription(description)
                    titleError = titleValidation.errorMessage
                    descriptionError = descValidation.errorMessage
                    if (titleValidation.isValid && descValidation.isValid) {
                        onConfirm(
                            selectedType,
                            title.trim(),
                            description.trim(),
                            selectedPlant,
                            photoUri
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (editingEntry == null) "Guardar en bitácora" else "Guardar cambios",
                    fontWeight = FontWeight.Bold
                )
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
