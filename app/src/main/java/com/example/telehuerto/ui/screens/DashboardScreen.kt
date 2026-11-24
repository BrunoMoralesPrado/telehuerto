package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.CityOption
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.NetworkUiState
import com.example.telehuerto.data.Plant
import com.example.telehuerto.data.PlantStatus
import com.example.telehuerto.data.WeatherSnapshot
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.HeroPottedPlantIllustration
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
import com.example.telehuerto.ui.theme.LimeAccent
import com.example.telehuerto.ui.theme.NutrientGreen
import com.example.telehuerto.ui.theme.SkyWeather
import com.example.telehuerto.ui.theme.SkyWeatherBg
import com.example.telehuerto.ui.theme.StatusAttention
import com.example.telehuerto.ui.theme.StatusAttentionBg
import com.example.telehuerto.ui.theme.StatusHealthy
import com.example.telehuerto.ui.theme.StatusHealthyBg
import com.example.telehuerto.ui.theme.SunYellow
import com.example.telehuerto.ui.theme.SurfaceTinted
import com.example.telehuerto.ui.theme.SyncPending
import com.example.telehuerto.ui.theme.SyncPendingBg
import com.example.telehuerto.ui.theme.TextOnDark
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import com.example.telehuerto.util.Validators
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val activePlants = HuertoRepository.activePlants()
    val activeAlerts = HuertoRepository.activeAlerts()
    val arduinoDevice = HuertoRepository.arduinoDevice
    val selectedPlant: Plant? = HuertoRepository.getSelectedPlantOrNull()?.takeIf { it.isActive }
        ?: activePlants.firstOrNull()

    val healthyCount = activePlants.count { it.status == PlantStatus.SALUDABLE }
    val attentionCount = activePlants.count { it.status == PlantStatus.REQUIERE_ATENCION }

    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greetingPrefix = when (hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    var showArduinoModal by rememberSaveable { mutableStateOf(false) }
    var showSyncModal by rememberSaveable { mutableStateOf(false) }
    var showCitySearchDialog by rememberSaveable { mutableStateOf(false) }
    var showPlantSelectorDialog by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (HuertoRepository.weatherState is NetworkUiState.Idle) {
            HuertoRepository.refreshWeather()
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController, "dashboard") },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = BackgroundBeige
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Encabezado con saludo dinámico y botón de perfil / simulador / logout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MI HUERTO · ${HuertoRepository.currentCity.uppercase()}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$greetingPrefix, ${HuertoRepository.currentUserName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        shape = CircleShape,
                        color = GreenSoft,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .clickable { showArduinoModal = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Perfil, sensores y cerrar sesión",
                                tint = GreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Hero Card con gradiente GreenPrimary -> GreenDark e ilustración vectorial
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GreenPrimary, GreenDark)
                                )
                            )
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    ) {
                        if (activePlants.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.68f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "Huerto listo para sembrar",
                                        color = TextOnDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                                Text(
                                    text = "Comienza tu huerto",
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDark,
                                    lineHeight = 28.sp
                                )
                                Text(
                                    text = "Registra tu primera planta para monitorear humedad, luz y nutrientes.",
                                    color = TextOnDark.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Button(
                                    onClick = { navController.navigate("add_plant") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LimeAccent,
                                        contentColor = GreenDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Añadir planta",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            val hasAlerts = activeAlerts.isNotEmpty()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.68f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                                    if (hasAlerts) Color(0xFFFFB4A9) else LimeAccent,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (hasAlerts) {
                                                "${activeAlerts.size} alertas activas"
                                            } else {
                                                "Todo en equilibrio"
                                            },
                                            color = TextOnDark,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Text(
                                    text = "Tu huerto está\ncreciendo bien",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDark,
                                    lineHeight = 29.sp
                                )

                                Text(
                                    text = "${activePlants.size} plantas conectadas · ${HuertoRepository.currentCity}",
                                    color = TextOnDark.copy(alpha = 0.84f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        HeroPottedPlantIllustration(
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
            }

            // 3. Franja compacta: Respaldo Volley + Simulador de sensores
            item {
                val pendingSync = HuertoRepository.pendingSyncCount()
                val syncedPct = HuertoRepository.syncedPercentage()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showSyncModal = true },
                        shape = RoundedCornerShape(16.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (pendingSync == 0) StatusHealthyBg else SyncPendingBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (pendingSync == 0) {
                                        Icons.Outlined.CloudDone
                                    } else {
                                        Icons.Outlined.CloudSync
                                    },
                                    contentDescription = "Centro de sincronización",
                                    tint = if (pendingSync == 0) StatusHealthy else SyncPending,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Respaldo: $syncedPct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (pendingSync == 0) {
                                        "SQLite + Volley al día"
                                    } else {
                                        "$pendingSync pendientes"
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showArduinoModal = true },
                        shape = RoundedCornerShape(16.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Sensors,
                                    contentDescription = "Simulador de sensores",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Simulador de sensores",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Nodo ${arduinoDevice.id} · ${arduinoDevice.temperatureCelsius}°C",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 4. Tarjeta "Clima de tu huerto (Volley)"
            item {
                WeatherVolleyCard(
                    weatherState = HuertoRepository.weatherState,
                    currentCity = HuertoRepository.currentCity,
                    onRefresh = { HuertoRepository.refreshWeather() },
                    onChangeCity = { showCitySearchDialog = true }
                )
            }

            // 5. Encabezado de Condiciones actuales + Selector explícito de planta
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                text = "Lecturas del cultivo seleccionado",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.DeviceThermostat,
                                contentDescription = "Temperatura",
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${arduinoDevice.temperatureCelsius}°C",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    if (selectedPlant != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardBackground,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (activePlants.size > 1) {
                                        showPlantSelectorDialog = true
                                    } else {
                                        navController.navigate("plant_detail")
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PlantAvatar(
                                        iconType = selectedPlant.iconType,
                                        photoUri = selectedPlant.photoUri,
                                        modifier = Modifier.size(34.dp),
                                        cornerRadius = 10.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Mostrando: ${selectedPlant.name}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${selectedPlant.location} · ${selectedPlant.sensorChannel}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (activePlants.size > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = GreenSoft
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.SwapHoriz,
                                                contentDescription = "Cambiar planta",
                                                tint = GreenPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Cambiar",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GreenPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Cuadrícula 2x2 de Condiciones o estado vacío seguro
            item {
                if (selectedPlant == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Sin plantas activas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Agrega una planta a tu huerto para visualizar sus lecturas de humedad, iluminación y nutrientes N-P-K.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 17.sp
                            )
                            Button(
                                onClick = { navController.navigate("add_plant") },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir planta", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    val displayHumidity = selectedPlant.humidity
                    val humiditySubtitle = when {
                        displayHumidity < 30 -> "Crítico · regar de inmediato"
                        displayHumidity < HuertoRepository.HUMIDITY_MIN -> "Suelo seco · regar pronto"
                        displayHumidity <= 80 -> "Humedad óptima en raíces"
                        else -> "Exceso de agua · pausar riego"
                    }
                    val nutrientsSubtitle = if (selectedPlant.npk.n < HuertoRepository.NITROGEN_MIN) {
                        "N ${selectedPlant.npk.n}% bajo · fertilizar pronto"
                    } else {
                        "N ${selectedPlant.npk.n}% · P ${selectedPlant.npk.p}% · K ${selectedPlant.npk.k}%"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ConditionCard(
                                modifier = Modifier.weight(1f),
                                title = "Humedad",
                                value = "$displayHumidity%",
                                subtitle = humiditySubtitle,
                                icon = Icons.Outlined.WaterDrop,
                                iconTint = HumidityBlue,
                                iconContainerColor = SkyWeatherBg,
                                onClick = {
                                    HuertoRepository.selectPlant(selectedPlant.id)
                                    navController.navigate("plant_detail")
                                }
                            )
                            ConditionCard(
                                modifier = Modifier.weight(1f),
                                title = "Iluminación",
                                value = selectedPlant.lightLevelText,
                                subtitle = selectedPlant.sunHoursText,
                                icon = Icons.Outlined.WbSunny,
                                iconTint = SunYellow,
                                iconContainerColor = SyncPendingBg,
                                onClick = {
                                    HuertoRepository.selectPlant(selectedPlant.id)
                                    navController.navigate("plant_detail")
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ConditionCard(
                                modifier = Modifier.weight(1f),
                                title = "Nutrientes",
                                value = selectedPlant.npk.statusLabel,
                                subtitle = nutrientsSubtitle,
                                icon = Icons.Outlined.Science,
                                iconTint = NutrientGreen,
                                iconContainerColor = GreenSoft,
                                onClick = {
                                    HuertoRepository.selectPlant(selectedPlant.id)
                                    navController.navigate("plant_detail")
                                }
                            )
                            ConditionCard(
                                modifier = Modifier.weight(1f),
                                title = "Plantas",
                                value = "${activePlants.size}",
                                subtitle = "$healthyCount sanas · $attentionCount requiere atención",
                                icon = Icons.Outlined.Eco,
                                iconTint = GreenPrimary,
                                iconContainerColor = GreenSoft,
                                onClick = { navController.navigate("plants") }
                            )
                        }
                    }
                }
            }

            // 7. Tarjeta Próximo Riego
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SkyWeatherBg,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.WaterDrop,
                                        contentDescription = null,
                                        tint = SkyWeather,
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

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                val currentActive = HuertoRepository.activePlants()
                                val watered = HuertoRepository.waterAllNow()
                                val message = when {
                                    currentActive.isEmpty() -> "No hay plantas activas para regar."
                                    watered == 0 -> "Todas tus plantas activas ya tienen humedad suficiente (≥85%)."
                                    else -> "Riego completado en $watered planta(s)."
                                }
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSoft,
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
            }

            // 8. Banner inferior de alertas
            item {
                val alertCount = activeAlerts.size
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (alertCount > 0) AlertCriticalBg else StatusHealthyBg,
                    border = BorderStroke(
                        1.dp,
                        if (alertCount > 0) AlertCritical.copy(alpha = 0.35f) else BorderSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { navController.navigate("alerts") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (alertCount > 0) {
                                    Icons.Outlined.NotificationsActive
                                } else {
                                    Icons.Outlined.CheckCircleOutline
                                },
                                contentDescription = null,
                                tint = if (alertCount > 0) AlertCritical else GreenPrimary,
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
                                color = if (alertCount > 0) AlertCritical else GreenPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "Ver alertas",
                            tint = if (alertCount > 0) AlertCritical else GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showArduinoModal) {
        ArduinoSimDialog(
            onDismiss = { showArduinoModal = false },
            onOpenSyncCenter = {
                showArduinoModal = false
                showSyncModal = true
            },
            onLogout = {
                showArduinoModal = false
                HuertoRepository.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    if (showSyncModal) {
        SyncCenterDialog(
            onDismiss = { showSyncModal = false }
        )
    }

    if (showCitySearchDialog) {
        CitySearchDialog(
            currentCity = HuertoRepository.currentCity,
            onDismiss = { showCitySearchDialog = false }
        )
    }

    if (showPlantSelectorDialog) {
        PlantSelectorDialog(
            plants = activePlants,
            selectedPlantId = selectedPlant?.id ?: "",
            onSelect = { plant ->
                HuertoRepository.selectPlant(plant.id)
                showPlantSelectorDialog = false
            },
            onDismiss = { showPlantSelectorDialog = false }
        )
    }
}

@Composable
private fun WeatherVolleyCard(
    weatherState: NetworkUiState<WeatherSnapshot>,
    currentCity: String,
    onRefresh: () -> Unit,
    onChangeCity: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SkyWeatherBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = null,
                            tint = SkyWeather,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Clima de tu huerto (Volley)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = SkyWeather,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = currentCity,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onChangeCity,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Cambiar ciudad",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkyWeather
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Actualizar clima",
                            tint = SkyWeather,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            when (weatherState) {
                is NetworkUiState.Idle, is NetworkUiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = SkyWeather
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Consultando clima en Open-Meteo...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                is NetworkUiState.Success -> {
                    WeatherSnapshotContent(snapshot = weatherState.data, isCachedFallback = false)
                }

                is NetworkUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = StatusAttentionBg,
                        border = BorderStroke(1.dp, StatusAttention.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudOff,
                                    contentDescription = null,
                                    tint = StatusAttention,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = weatherState.message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = onRefresh,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusAttention,
                                        contentColor = TextOnDark
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reintentar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    val cached = weatherState.cachedData
                    if (cached != null) {
                        WeatherSnapshotContent(snapshot = cached, isCachedFallback = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherSnapshotContent(
    snapshot: WeatherSnapshot,
    isCachedFallback: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (isCachedFallback || snapshot.isFromCache) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceTinted,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Text(
                    text = "Última lectura guardada · ${snapshot.fetchedAtText}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${snapshot.temperatureC.toInt()}°C",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.padding(bottom = 3.dp)) {
                    Text(
                        text = snapshot.conditionLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SkyWeather
                    )
                    Text(
                        text = snapshot.city,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherMetricPill(
                label = "Humedad ${snapshot.ambientHumidity}%",
                modifier = Modifier.weight(1f)
            )
            WeatherMetricPill(
                label = "UV ${snapshot.uvIndex}",
                modifier = Modifier.weight(1f)
            )
            WeatherMetricPill(
                label = "Lluvia ${snapshot.rainProbability}%",
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SkyWeatherBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = null,
                    tint = SkyWeather,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = snapshot.gardenAdvice,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun WeatherMetricPill(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceTinted,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun CitySearchDialog(
    currentCity: String,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf(currentCity) }
    var queryError by rememberSaveable { mutableStateOf("") }
    val searchState = HuertoRepository.citySearchState

    fun runSearch() {
        val check = Validators.validateCityName(query)
        if (!check.isValid) {
            queryError = check.errorMessage ?: "Ingresa una ciudad válida."
            return
        }
        queryError = ""
        HuertoRepository.searchCities(query)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cambiar ciudad del huerto",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Busca tu localidad en Open-Meteo para ajustar el pronóstico y consejos de riego",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (queryError.isNotEmpty()) {
                                val res = Validators.validateCityName(it)
                                queryError = if (res.isValid) "" else (res.errorMessage ?: "")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Ej. Guadalajara, Madrid...", fontSize = 13.sp) },
                        isError = queryError.isNotEmpty(),
                        supportingText = if (queryError.isNotEmpty()) {
                            { Text(queryError, color = AlertCritical, fontSize = 11.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { runSearch() }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyWeather,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = SurfaceTinted,
                            unfocusedContainerColor = SurfaceTinted
                        )
                    )
                    Button(
                        onClick = { runSearch() },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyWeather),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(54.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Buscar ciudad",
                            tint = TextOnDark
                        )
                    }
                }

                when (searchState) {
                    is NetworkUiState.Idle -> {
                        Text(
                            text = "Escribe una ciudad y presiona buscar para consultar coordenadas en Open-Meteo.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    is NetworkUiState.Loading -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = SkyWeather
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Buscando localidades...",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    is NetworkUiState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AlertCriticalBg,
                            border = BorderStroke(1.dp, AlertCritical.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = searchState.message,
                                fontSize = 12.sp,
                                color = AlertCritical,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    is NetworkUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchState.data) { option ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceTinted,
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            HuertoRepository.selectCityAndFetchWeather(option)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = option.displayLabel(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "Lat ${"%.2f".format(option.latitude)} · Lon ${"%.2f".format(option.longitude)}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Outlined.ChevronRight,
                                            contentDescription = null,
                                            tint = SkyWeather,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@Composable
private fun PlantSelectorDialog(
    plants: List<Plant>,
    selectedPlantId: String,
    onSelect: (Plant) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Seleccionar planta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Elige qué cultivo mostrar en las tarjetas de condiciones actuales",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(plants, key = { it.id }) { plant ->
                    val isSelected = plant.id == selectedPlantId
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) GreenSoft else SurfaceTinted,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GreenPrimary else BorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelect(plant) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PlantAvatar(
                                iconType = plant.iconType,
                                photoUri = plant.photoUri,
                                modifier = Modifier.size(38.dp),
                                cornerRadius = 10.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plant.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Humedad ${plant.humidity}% · Luz ${plant.lightLevelText} · NPK ${plant.npk.statusLabel}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
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
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, BorderSubtle)
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
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
