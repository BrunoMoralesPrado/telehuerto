package com.example.telehuerto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PlantPotIllustration
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val DetWaterColor = Color(0xFF3B7A8C)
private val DetSunColor = Color(0xFFB58938)
private val DetSunBar = Color(0xFFDEB456)
private val DetNpkColor = Color(0xFFB55B52)
private val DetNpkBar = Color(0xFFD66853)
private val DetSoftGreen = Color(0xFFDFEDE2)
private val DetAttentionBg = Color(0xFFF9E3DC)
private val DetAttentionFg = Color(0xFFC94A38)
private val DetRetiredBg = Color(0xFFE6E8E4)
private val DetTrack = Color(0xFFE8ECE3)
private val DetDanger = Color(0xFFC94A38)

private val RetireReasons = listOf(
    "Ciclo de cosecha finalizado",
    "Planta trasplantada fuera del huerto",
    "Marchitamiento / plaga"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController) {
    val plant = HuertoRepository.getSelectedPlant()
    var showRetireDialog by remember { mutableStateOf(false) }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top bar
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Volver", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Text(
                    "DETALLES DE LA PLANTA",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = TextPrimary
                )
                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Opciones", tint = TextPrimary)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (plant.isActive) {
                            DropdownMenuItem(
                                text = { Text("Dar de baja del huerto", color = DetDanger) },
                                onClick = { showMenu = false; showRetireDialog = true }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Restaurar planta al huerto") },
                                onClick = {
                                    showMenu = false
                                    HuertoRepository.restorePlant(plant.id)
                                    scope.launch { snackbarHostState.showSnackbar("${plant.name} volvió al huerto") }
                                }
                            )
                        }
                    }
                }
            }

            // Header card
            Surface(shape = RoundedCornerShape(24.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    PlantPotIllustration(iconType = plant.iconType, modifier = Modifier.size(84.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(plant.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(plant.species, fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        val (bg, fg) = when (plant.status) {
                            PlantStatus.SALUDABLE -> DetSoftGreen to GreenPrimary
                            PlantStatus.REQUIERE_ATENCION -> DetAttentionBg to DetAttentionFg
                            PlantStatus.DADA_DE_BAJA -> DetRetiredBg to TextSecondary
                        }
                        Box(modifier = Modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                            Text("● ${plant.status.label}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("🔗 ${plant.arduinoId} · ${plant.sensorChannel}", fontSize = 11.sp, color = TextSecondary)
                        Text("Ubicación: ${plant.location}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            // Humedad
            MetricCard {
                MetricHeader(Icons.Default.WaterDrop, DetWaterColor, "Humedad del suelo", "${plant.humidity}%", DetWaterColor)
                Spacer(Modifier.height(10.dp))
                SimpleBar(plant.humidity / 100f, DetWaterColor)
                Spacer(Modifier.height(10.dp))
                Text(HuertoRepository.interpretHumidity(plant.humidity), fontSize = 13.sp, color = TextSecondary)
            }

            // Iluminación
            MetricCard {
                MetricHeader(Icons.Default.WbSunny, DetSunColor, "Iluminación", plant.lightLevelText, DetSunColor)
                Spacer(Modifier.height(10.dp))
                SimpleBar(plant.sunPercentage / 100f, DetSunBar)
                Spacer(Modifier.height(10.dp))
                Text(HuertoRepository.interpretLight(plant.sunPercentage, plant.sunHoursText), fontSize = 13.sp, color = TextSecondary)
            }

            // NPK
            MetricCard {
                MetricHeader(Icons.Default.Equalizer, DetNpkColor, "Nutrientes N–P–K", plant.npk.statusLabel, DetNpkColor)
                Spacer(Modifier.height(10.dp))
                NpkRow("N", plant.npk.n)
                Spacer(Modifier.height(6.dp))
                NpkRow("P", plant.npk.p)
                Spacer(Modifier.height(6.dp))
                NpkRow("K", plant.npk.k)
                Spacer(Modifier.height(10.dp))
                Text(HuertoRepository.interpretNPK(plant.npk), fontSize = 13.sp, color = TextSecondary)
            }

            // Quick actions
            if (plant.isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            HuertoRepository.waterPlant(plant.id)
                            scope.launch { snackbarHostState.showSnackbar("Riego registrado para ${plant.name}") }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DetWaterColor, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("💧 Regar planta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
                    Button(
                        onClick = {
                            HuertoRepository.fertilizePlant(plant.id)
                            scope.launch { snackbarHostState.showSnackbar("Abono N-P-K aplicado a ${plant.name}") }
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("🌱 Abonar N-P-K", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
                }
            }

            // Activity
            Text("Actividad reciente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Historial de esta planta", fontSize = 12.sp, color = TextSecondary, modifier = Modifier.offset(y = (-10).dp))
            Surface(shape = RoundedCornerShape(24.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    if (plant.recentActivities.isEmpty()) {
                        Text("Todavía no hay actividad registrada.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 14.dp))
                    }
                    plant.recentActivities.forEachIndexed { index, activity ->
                        if (index > 0) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DetTrack))
                        Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(DetSoftGreen, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (activity.isAlert) Icons.Default.NotificationsNone else Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(activity.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(activity.time, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Retire / restore
            if (plant.isActive) {
                OutlinedButton(
                    onClick = { showRetireDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DetDanger)
                ) { Text("Dar de baja del huerto", fontWeight = FontWeight.SemiBold) }
            } else {
                if (plant.retiredReason != null) {
                    Text("Motivo de baja: ${plant.retiredReason}", fontSize = 13.sp, color = TextSecondary)
                }
                Button(
                    onClick = {
                        HuertoRepository.restorePlant(plant.id)
                        scope.launch { snackbarHostState.showSnackbar("${plant.name} volvió al huerto") }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
                ) { Text("Restaurar planta al huerto", fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(8.dp))
        }
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
}

@Composable
private fun RetirePlantDialog(
    plantName: String,
    sensorChannel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selected by remember { mutableStateOf(RetireReasons[0]) }
    var otherSelected by remember { mutableStateOf(false) }
    var otherText by remember { mutableStateOf("") }
    val reason = if (otherSelected) otherText.trim() else selected

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Dar de baja \"$plantName\"", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "El canal del sensor Arduino ($sensorChannel) quedará libre y la planta dejará de aparecer como activa en el sistema. Podrás restaurarla después.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text("Motivo de la baja", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                RetireReasons.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = !otherSelected && selected == option, role = Role.RadioButton) {
                                otherSelected = false
                                selected = option
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !otherSelected && selected == option,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = otherSelected, role = Role.RadioButton) { otherSelected = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = otherSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Otro motivo", fontSize = 14.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 8.dp))
                }
                if (otherSelected) {
                    OutlinedTextField(
                        value = otherText,
                        onValueChange = { otherText = it },
                        placeholder = { Text("Escribe el motivo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DetDanger, contentColor = Color.White)
            ) { Text("Dar de baja") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextSecondary) }
        }
    )
}

@Composable
private fun MetricCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun MetricHeader(icon: ImageVector, iconTint: Color, title: String, value: String, valueColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun NpkRow(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.width(18.dp))
        SimpleBar(value / 100f, DetNpkBar, modifier = Modifier.weight(1f))
        Text("$value%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, textAlign = TextAlign.End, modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun SimpleBar(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(DetTrack)
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
