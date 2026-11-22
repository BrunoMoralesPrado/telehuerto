package com.example.telehuerto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
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
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.HeroPottedPlantIllustration
import com.example.telehuerto.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val activePlants = HuertoRepository.activePlants()
    val activeAlerts = HuertoRepository.activeAlerts()
    val arduinoDevice = HuertoRepository.arduinoDevice
    val selectedPlant = HuertoRepository.getSelectedPlant()

    val healthyCount = activePlants.count { it.status == PlantStatus.SALUDABLE }
    val attentionCount = activePlants.count { it.status == PlantStatus.REQUIERE_ATENCION }

    val displayHumidity = selectedPlant.humidity
    val humiditySubtitle = when {
        displayHumidity < 30 -> "Crítico · regar de inmediato"
        displayHumidity <= 45 -> "Suelo seco · regar pronto"
        displayHumidity <= 80 -> "Humedad óptima en raíces"
        else -> "Exceso de agua · pausar riego"
    }
    val nutrientsSubtitle = if (selectedPlant.npk.statusLabel.equals("Bajos", ignoreCase = true)) {
        "Fertilizar en 2 días"
    } else {
        "NPK en nivel óptimo"
    }

    val isSevereEmergency = activePlants.any { it.humidity < 30 }

    var showArduinoModal by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { BottomNavBar(navController, "dashboard") },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundBeige
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 36.dp, bottom = 28.dp)
        ) {
            // Top Greeting Header (Figma Image #2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MI HUERTO",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Buenos días, ${HuertoRepository.currentUserName}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDCE7D6),
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { showArduinoModal = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Perfil y Nodo Arduino",
                                tint = GreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Hero Card with Potted Plant Illustration (Figma Image #2)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.68f),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isSevereEmergency) Color(0xFFFFB4A9) else Color(0xFFB7E075),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSevereEmergency) {
                                            "${activeAlerts.size} alertas activas"
                                        } else {
                                            "Todo en equilibrio"
                                        },
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Text(
                                text = if (isSevereEmergency) {
                                    "Tu huerto pide\nriego urgente"
                                } else {
                                    "Tu huerto está\ncreciendo bien"
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 29.sp
                            )

                            Text(
                                text = "${activePlants.size} plantas conectadas ·\nactualizado ${arduinoDevice.lastSyncText}",
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        HeroPottedPlantIllustration(
                            modifier = Modifier
                                .size(124.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Subtle Interactive IoT Node Status Chip (Opens ArduinoSimSheet)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE3ECE0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showArduinoModal = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${arduinoDevice.model} (${arduinoDevice.id}) · Simular sensores",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Abrir simulador Arduino",
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Condiciones actuales Header (Figma Image #2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Condiciones actuales",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Lecturas en tiempo real",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeviceThermostat,
                            contentDescription = "Temperatura",
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${arduinoDevice.temperatureCelsius}°",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2x2 Grid of ConditionCards (Figma Image #2)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ConditionCard(
                        modifier = Modifier.weight(1f),
                        title = "Humedad",
                        value = "$displayHumidity%",
                        subtitle = humiditySubtitle,
                        icon = Icons.Default.WaterDrop,
                        iconTint = Color(0xFF2B7A88),
                        iconContainerColor = Color(0xFFDFEFF2),
                        onClick = { navController.navigate("plant_detail") }
                    )
                    ConditionCard(
                        modifier = Modifier.weight(1f),
                        title = "Iluminación",
                        value = selectedPlant.lightLevelText,
                        subtitle = selectedPlant.sunHoursText,
                        icon = Icons.Default.WbSunny,
                        iconTint = Color(0xFFB8862D),
                        iconContainerColor = Color(0xFFF9EDD4),
                        onClick = { navController.navigate("plant_detail") }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ConditionCard(
                        modifier = Modifier.weight(1f),
                        title = "Nutrientes",
                        value = selectedPlant.npk.statusLabel,
                        subtitle = nutrientsSubtitle,
                        icon = Icons.Default.Equalizer,
                        iconTint = Color(0xFFB55B52),
                        iconContainerColor = Color(0xFFF7E1DC),
                        onClick = { navController.navigate("plant_detail") }
                    )
                    ConditionCard(
                        modifier = Modifier.weight(1f),
                        title = "Plantas",
                        value = "${activePlants.size}",
                        subtitle = "$healthyCount sanas · $attentionCount requiere atención",
                        icon = Icons.Default.Eco,
                        iconTint = GreenPrimary,
                        iconContainerColor = Color(0xFFE2EDE0),
                        onClick = { navController.navigate("plants") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Próximo Riego Card (Figma Image #2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDFEFF2),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF2B7A88),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "PRÓXIMO RIEGO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = HuertoRepository.nextWateringText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                HuertoRepository.waterAllNow()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Riego activado en el nodo ${arduinoDevice.id}. Humedad actualizada."
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE2EDE0),
                                contentColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Regar ahora",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bottom Alert Banner matching Figma Image #2
            item {
                val alertCount = activeAlerts.size
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (alertCount > 0) Color(0xFFF9E3DC) else Color(0xFFE2EDE0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("alerts") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (alertCount > 0) {
                                    Icons.Outlined.NotificationsActive
                                } else {
                                    Icons.Default.CheckCircleOutline
                                },
                                contentDescription = null,
                                tint = if (alertCount > 0) Color(0xFFB55B52) else GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (alertCount > 0) {
                                    "$alertCount alertas requieren atención"
                                } else {
                                    "Sin alertas pendientes · Todo al día"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (alertCount > 0) Color(0xFF8C3D35) else GreenPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver alertas",
                            tint = if (alertCount > 0) Color(0xFFB55B52) else GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showArduinoModal) {
        ArduinoSimDialog(onDismiss = { showArduinoModal = false })
    }
}

@Composable
fun ConditionCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconContainerColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .height(138.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconContainerColor,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
