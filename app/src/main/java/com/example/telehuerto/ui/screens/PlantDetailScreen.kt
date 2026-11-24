package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PauseCircleOutline
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.data.SyncStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PlantAvatar
import com.example.telehuerto.ui.theme.AlertCritical
import com.example.telehuerto.ui.theme.AlertCriticalBg
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.BorderSubtle
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.DividerColor
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.GreenSoft
import com.example.telehuerto.ui.theme.HumidityBlue
import com.example.telehuerto.ui.theme.NutrientGreen
import com.example.telehuerto.ui.theme.SkyWeather
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
import kotlinx.coroutines.launch

private val RetireReasons = listOf(
    "Ciclo de cosecha finalizado",
    "Planta trasplantada fuera del huerto",
    "Marchitamiento / plaga"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: String? = null
) {
    LaunchedEffect(plantId) {
        if (!plantId.isNullOrBlank()) {
            HuertoRepository.selectPlant(plantId)
        }
    }

    val plant = if (!plantId.isNullOrBlank()) {
        HuertoRepository.getPlantById(plantId)
    } else {
        HuertoRepository.getSelectedPlantOrNull()
    }

    var showRetireDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun goToPlants() {
        navController.navigate("plants") {
            popUpTo("plants") { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = BackgroundBeige,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController, "plants") }
    ) { padding ->
        if (plant == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceTinted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = "Esta planta ya no está disponible",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Puede que haya sido eliminada del huerto. Vuelve a tu colección para ver las plantas activas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { goToPlants() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = TextOnDark
                            )
                        ) {
                            Text(
                                text = "Volver a Mis plantas",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Volver",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "DETALLES DE LA PLANTA",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = TextPrimary
                )

                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Opciones de planta",
                                tint = TextPrimary
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar planta", color = TextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                navController.navigate("edit_plant/${plant.id}")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sincronizar respaldo", color = TextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CloudSync,
                                    contentDescription = null,
                                    tint = SkyWeather,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                HuertoRepository.syncPendingItems()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        HuertoRepository.lastSyncMessage ?: "Respaldo verificado."
                                    )
                                }
                            }
                        )
                        if (plant.isActive) {
                            DropdownMenuItem(
                                text = { Text("Dar de baja del huerto", color = StatusAttention) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.PauseCircleOutline,
                                        contentDescription = null,
                                        tint = StatusAttention,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showRetireDialog = true
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Restaurar planta", color = GreenPrimary) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Restore,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    HuertoRepository.restorePlant(plant.id)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("${plant.name} volvió al huerto.")
                                    }
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Eliminar permanentemente", color = AlertCritical) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = null,
                                    tint = AlertCritical,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Header Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlantAvatar(
                        iconType = plant.iconType,
                        photoUri = plant.photoUri,
                        modifier = Modifier.size(88.dp),
                        cornerRadius = 20.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = plant.species,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DetailStatusPill(plant.status)
                            DetailSyncPill(plant.syncStatus)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${plant.arduinoId} · ${plant.sensorChannel}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${plant.location} · Desde ${plant.plantedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Humedad
            MetricCard {
                MetricHeader(
                    icon = Icons.Outlined.WaterDrop,
                    iconTint = HumidityBlue,
                    title = "Humedad del suelo",
                    value = "${plant.humidity}%",
                    valueColor = SkyWeather
                )
                Spacer(modifier = Modifier.height(10.dp))
                SimpleBar(progress = plant.humidity / 100f, color = HumidityBlue)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = HuertoRepository.interpretHumidity(plant.humidity),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Iluminación
            MetricCard {
                MetricHeader(
                    icon = Icons.Outlined.WbSunny,
                    iconTint = SunYellow,
                    title = "Iluminación",
                    value = plant.lightLevelText,
                    valueColor = StatusAttention
                )
                Spacer(modifier = Modifier.height(10.dp))
                SimpleBar(progress = plant.sunPercentage / 100f, color = SunYellow)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = HuertoRepository.interpretLight(plant.sunPercentage, plant.sunHoursText),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Nutrientes N–P–K
            MetricCard {
                MetricHeader(
                    icon = Icons.Default.Equalizer,
                    iconTint = NutrientGreen,
                    title = "Nutrientes N–P–K",
                    value = plant.npk.statusLabel,
                    valueColor = GreenPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                NpkRow(label = "N", value = plant.npk.n)
                Spacer(modifier = Modifier.height(8.dp))
                NpkRow(label = "P", value = plant.npk.p)
                Spacer(modifier = Modifier.height(8.dp))
                NpkRow(label = "K", value = plant.npk.k)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = HuertoRepository.interpretNPK(plant.npk),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Acciones rápidas de cuidado (cero emojis)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val ok = HuertoRepository.waterPlant(plant.id)
                        scope.launch {
                            if (!ok) {
                                snackbarHostState.showSnackbar(
                                    "El sustrato ya tiene humedad suficiente (≥85%) o está dada de baja."
                                )
                            } else {
                                snackbarHostState.showSnackbar("Riego registrado (${plant.name}).")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyWeather,
                        contentColor = TextOnDark
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Regar planta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        if (!plant.isActive) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Restaura la planta antes de registrar abono.")
                            }
                        } else {
                            HuertoRepository.fertilizePlant(plant.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Abono N-P-K aplicado a ${plant.name}.")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = TextOnDark
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Abonar N-P-K",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Actividad reciente
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Actividad reciente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Historial de esta planta",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    if (plant.recentActivities.isEmpty()) {
                        Text(
                            text = "Todavía no hay actividad registrada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 14.dp)
                        )
                    } else {
                        plant.recentActivities.forEachIndexed { index, activity ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(DividerColor)
                                )
                            }
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (activity.isAlert) StatusAttentionBg else GreenSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (activity.isAlert) {
                                            Icons.Outlined.NotificationsActive
                                        } else {
                                            Icons.Outlined.WaterDrop
                                        },
                                        contentDescription = null,
                                        tint = if (activity.isAlert) StatusAttention else GreenPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activity.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = activity.time,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gestión de planta
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardBackground,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Gestión de planta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!plant.isActive && !plant.retiredReason.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceTinted,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Motivo de baja: ${plant.retiredReason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { navController.navigate("edit_plant/${plant.id}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.45f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Editar datos", fontWeight = FontWeight.SemiBold)
                    }

                    if (plant.isActive) {
                        OutlinedButton(
                            onClick = { showRetireDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, StatusAttention.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAttention)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PauseCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Dar de baja del huerto", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = {
                                HuertoRepository.restorePlant(plant.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("${plant.name} volvió al huerto.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = TextOnDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Restore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Restaurar planta", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertCriticalBg,
                            contentColor = AlertCritical
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Eliminar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        if (showRetireDialog) {
            RetirePlantDialog(
                plantName = plant.name,
                sensorChannel = plant.sensorChannel,
                onDismiss = { showRetireDialog = false },
                onConfirm = { reason ->
                    showRetireDialog = false
                    HuertoRepository.retirePlant(plant.id, reason)
                    goToPlants()
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = CardBackground,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Eliminar \"${plant.name}\"",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Text(
                        text = "Esta acción borrará la planta y sus alertas de forma permanente. Las notas de la bitácora se conservarán como historial.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            HuertoRepository.deletePlantPermanently(plant.id)
                            goToPlants()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AlertCritical,
                            contentColor = TextOnDark
                        )
                    ) {
                        Text(text = "Eliminar", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = "Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailStatusPill(status: PlantStatus) {
    val (bg, fg) = when (status) {
        PlantStatus.SALUDABLE -> StatusHealthyBg to StatusHealthy
        PlantStatus.REQUIERE_ATENCION -> StatusAttentionBg to StatusAttention
        PlantStatus.DADA_DE_BAJA -> StatusRetiredBg to StatusRetired
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
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
            color = fg
        )
    }
}

@Composable
private fun DetailSyncPill(syncStatus: SyncStatus) {
    val (bg, fg, icon) = when (syncStatus) {
        SyncStatus.SYNCED -> Triple(StatusHealthyBg.copy(alpha = 0.7f), StatusHealthy, Icons.Outlined.CloudDone)
        SyncStatus.PENDING -> Triple(SyncPendingBg, SyncPending, Icons.Outlined.CloudUpload)
        SyncStatus.ERROR -> Triple(AlertCriticalBg, AlertCritical, Icons.Outlined.CloudOff)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
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
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun RetirePlantDialog(
    plantName: String,
    sensorChannel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(RetireReasons[0]) }
    var otherSelected by rememberSaveable { mutableStateOf(false) }
    var otherText by rememberSaveable { mutableStateOf("") }
    var showOtherError by rememberSaveable { mutableStateOf(false) }
    val reason = if (otherSelected) otherText.trim() else selected

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Dar de baja \"$plantName\"",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "El canal del sensor ($sensorChannel) quedará libre y la planta pasará a la lista de bajas. Podrás restaurarla cuando quieras.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Motivo de la baja",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                RetireReasons.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = !otherSelected && selected == option,
                                role = Role.RadioButton
                            ) {
                                otherSelected = false
                                showOtherError = false
                                selected = option
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !otherSelected && selected == option,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = otherSelected,
                            role = Role.RadioButton
                        ) {
                            otherSelected = true
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = otherSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Otro motivo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                if (otherSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = otherText,
                        onValueChange = {
                            otherText = it
                            if (it.isNotBlank()) showOtherError = false
                        },
                        placeholder = { Text("Escribe el motivo", color = TextSecondary) },
                        isError = showOtherError && otherText.isBlank(),
                        supportingText = if (showOtherError && otherText.isBlank()) {
                            { Text("Describe el motivo para continuar.", color = AlertCritical) }
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            errorBorderColor = AlertCritical
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (otherSelected && otherText.isBlank()) {
                        showOtherError = true
                    } else {
                        onConfirm(reason)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusAttention,
                    contentColor = TextOnDark
                )
            ) {
                Text(text = "Dar de baja", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun MetricCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun MetricHeader(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    valueColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun NpkRow(label: String, value: Int) {
    val barColor = when {
        value < 30 -> AlertCritical
        value < 60 -> SunYellow
        else -> NutrientGreen
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.width(20.dp)
        )
        SimpleBar(
            progress = value / 100f,
            color = barColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$value%",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp)
        )
    }
}

@Composable
private fun SimpleBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(DividerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}
