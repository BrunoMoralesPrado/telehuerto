package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.NPKValues
import com.example.telehuerto.data.PlantStatus
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
import com.example.telehuerto.ui.theme.SkyWeatherBg
import com.example.telehuerto.ui.theme.StatusAttention
import com.example.telehuerto.ui.theme.StatusAttentionBg
import com.example.telehuerto.ui.theme.StatusHealthyBg
import com.example.telehuerto.ui.theme.SunYellow
import com.example.telehuerto.ui.theme.SurfaceTinted
import com.example.telehuerto.ui.theme.SyncPendingBg
import com.example.telehuerto.ui.theme.TextOnDark
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ArduinoSimDialog(
    onDismiss: () -> Unit,
    onOpenSyncCenter: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val activePlants = HuertoRepository.activePlants()
    val device = HuertoRepository.arduinoDevice

    var selectedPlantId by rememberSaveable {
        mutableStateOf(
            activePlants.firstOrNull { it.id == HuertoRepository.selectedPlantId }?.id
                ?: activePlants.firstOrNull()?.id
                ?: ""
        )
    }

    val currentPlant = HuertoRepository.getPlantById(selectedPlantId)?.takeIf { it.isActive }
        ?: activePlants.firstOrNull()

    var humidity by rememberSaveable(currentPlant?.id) {
        mutableIntStateOf(currentPlant?.humidity ?: 68)
    }
    var sunPercentage by rememberSaveable(currentPlant?.id) {
        mutableIntStateOf(currentPlant?.sunPercentage ?: 72)
    }
    var nitrogen by rememberSaveable(currentPlant?.id) {
        mutableIntStateOf(currentPlant?.npk?.n ?: 70)
    }
    var phosphorus by rememberSaveable(currentPlant?.id) {
        mutableIntStateOf(currentPlant?.npk?.p ?: 68)
    }
    var potassium by rememberSaveable(currentPlant?.id) {
        mutableIntStateOf(currentPlant?.npk?.k ?: 74)
    }
    var temperature by rememberSaveable {
        mutableIntStateOf(device.temperatureCelsius)
    }
    var feedbackBanner by rememberSaveable {
        mutableStateOf("")
    }

    val previewNpkLabel = HuertoRepository.npkLabelFor(nitrogen, phosphorus, potassium)
    val previewNpk = NPKValues(
        n = nitrogen,
        p = phosphorus,
        k = potassium,
        statusLabel = previewNpkLabel
    )
    val previewHoursText = HuertoRepository.sunHoursFor(sunPercentage)
    val previewLightLevel = HuertoRepository.lightLevelFor(sunPercentage)
    val previewStatus = HuertoRepository.statusFor(humidity, nitrogen, sunPercentage)
    val willNeedAttention = previewStatus == PlantStatus.REQUIERE_ATENCION

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundBeige),
            border = BorderStroke(1.dp, BorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Encabezado del diálogo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GreenPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Memory,
                                    contentDescription = "Simulador de sensores",
                                    tint = TextOnDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PERFIL Y TELEMETRÍA LOCAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Simulador de sensores (datos de prueba)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary
                        )
                    }
                }

                // Sección 1: Información de cuenta, nodo vinculado, Centro Volley y Cerrar sesión
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CUENTA Y NODO SIMULADO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = StatusHealthyBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(GreenPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Serie ${device.id}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonOutline,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${HuertoRepository.currentUserName} (${HuertoRepository.currentUserEmail})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Ciudad: ${HuertoRepository.currentCity} · ${device.model} (${device.id})",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = "No necesitas hardware físico conectado: ajusta los controles de abajo para inyectar lecturas de prueba en SQLite y verificar alertas en vivo.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenSyncCenter,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = SurfaceTinted,
                                    contentColor = SkyWeather
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Centro de sincronización Volley",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onLogout,
                                modifier = Modifier.weight(0.85f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AlertCritical.copy(alpha = 0.4f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = AlertCriticalBg.copy(alpha = 0.5f),
                                    contentColor = AlertCritical
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cerrar sesión",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (activePlants.isEmpty() || currentPlant == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Eco,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "No hay plantas activas para simular",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Registra o reactiva una planta en tu huerto para enviarle lecturas de prueba.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    // Sección 2: Selector de planta activa
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "1. PLANTA ACTIVA A SIMULAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activePlants.forEach { plant ->
                                val isSelected = plant.id == currentPlant.id
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) GreenPrimary else CardBackground,
                                    border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedPlantId = plant.id
                                            feedbackBanner = ""
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Eco,
                                            contentDescription = null,
                                            tint = if (isSelected) TextOnDark else GreenPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = plant.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) TextOnDark else TextPrimary
                                            )
                                            Text(
                                                text = plant.sensorChannel,
                                                fontSize = 10.sp,
                                                color = if (isSelected) TextOnDark.copy(alpha = 0.8f) else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sección 3: Escenarios rápidos (Presets)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "2. ESCENARIOS RÁPIDOS DE PRUEBA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickScenarioChip(
                                icon = Icons.Outlined.WarningAmber,
                                label = "Sequía Crítica (${HuertoRepository.HUMIDITY_MIN - 17}%)",
                                tint = AlertCritical,
                                bg = AlertCriticalBg
                            ) {
                                humidity = 18
                                sunPercentage = 88
                                nitrogen = 38
                                phosphorus = 46
                                potassium = 52
                                temperature = 31
                                feedbackBanner = ""
                            }
                            QuickScenarioChip(
                                icon = Icons.Outlined.WaterDrop,
                                label = "Óptimo (78%)",
                                tint = SkyWeather,
                                bg = SkyWeatherBg
                            ) {
                                humidity = 78
                                sunPercentage = 76
                                nitrogen = 75
                                phosphorus = 72
                                potassium = 78
                                temperature = 24
                                feedbackBanner = ""
                            }
                            QuickScenarioChip(
                                icon = Icons.Outlined.Science,
                                label = "Déficit N-P-K (N ${HuertoRepository.NITROGEN_MIN - 10}%)",
                                tint = StatusAttention,
                                bg = StatusAttentionBg
                            ) {
                                humidity = 62
                                sunPercentage = 66
                                nitrogen = 20
                                phosphorus = 26
                                potassium = 28
                                temperature = 24
                                feedbackBanner = ""
                            }
                        }
                    }

                    // Sección 4: Controles deslizantes (Humedad, Sol, N, P, K, Temperatura)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "VALORES DE SENSORES (UMBRALES: HUM ≥${HuertoRepository.HUMIDITY_MIN}% · SOL ≥${HuertoRepository.SUN_MIN}% · N ≥${HuertoRepository.NITROGEN_MIN}%)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            SensorSliderRow(
                                title = "Humedad del sustrato",
                                valueText = "$humidity%",
                                rawText = if (humidity < HuertoRepository.HUMIDITY_MIN) {
                                    "Bajo el mínimo (${HuertoRepository.HUMIDITY_MIN}%) · generará alerta"
                                } else {
                                    "Rango saludable (mínimo ${HuertoRepository.HUMIDITY_MIN}%)"
                                },
                                value = humidity.toFloat(),
                                valueRange = 5f..98f,
                                activeColor = HumidityBlue,
                                onValueChange = {
                                    humidity = it.roundToInt()
                                    feedbackBanner = ""
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                            SensorSliderRow(
                                title = "Exposición solar",
                                valueText = "$sunPercentage% ($previewLightLevel)",
                                rawText = "$previewHoursText · mínimo recomendado ${HuertoRepository.SUN_MIN}%",
                                value = sunPercentage.toFloat(),
                                valueRange = 10f..100f,
                                activeColor = SunYellow,
                                onValueChange = {
                                    sunPercentage = it.roundToInt()
                                    feedbackBanner = ""
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                            SensorSliderRow(
                                title = "Nitrógeno (N)",
                                valueText = "N $nitrogen%",
                                rawText = if (nitrogen < HuertoRepository.NITROGEN_MIN) {
                                    "Déficit (<${HuertoRepository.NITROGEN_MIN}%) · requiere fertilizante"
                                } else {
                                    "Follaje activo (mínimo ${HuertoRepository.NITROGEN_MIN}%)"
                                },
                                value = nitrogen.toFloat(),
                                valueRange = 10f..95f,
                                activeColor = NutrientGreen,
                                onValueChange = {
                                    nitrogen = it.roundToInt()
                                    feedbackBanner = ""
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                            SensorSliderRow(
                                title = "Fósforo (P) y Potasio (K)",
                                valueText = "P $phosphorus% · K $potassium%",
                                rawText = "Nivel general N-P-K: $previewNpkLabel",
                                value = phosphorus.toFloat(),
                                valueRange = 10f..95f,
                                activeColor = GreenPrimary,
                                onValueChange = {
                                    val pVal = it.roundToInt()
                                    phosphorus = pVal
                                    potassium = (pVal + 6).coerceIn(10, 95)
                                    feedbackBanner = ""
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)

                            SensorSliderRow(
                                title = "Temperatura ambiental del nodo",
                                valueText = "$temperature°C",
                                rawText = if (temperature > 28) "Ambiente cálido" else "Rango templado (18–28°C)",
                                value = temperature.toFloat(),
                                valueRange = 12f..38f,
                                activeColor = StatusAttention,
                                onValueChange = {
                                    temperature = it.roundToInt()
                                    feedbackBanner = ""
                                }
                            )
                        }
                    }

                    // Sección 5: Interpretación directa con HuertoRepository
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (willNeedAttention) AlertCriticalBg else StatusHealthyBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (willNeedAttention) AlertCritical.copy(alpha = 0.35f) else BorderSubtle
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DIAGNÓSTICO PREVIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (willNeedAttention) AlertCritical else GreenPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CardBackground
                                ) {
                                    Text(
                                        text = previewStatus.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (willNeedAttention) AlertCritical else GreenPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            InterpretationRow(
                                icon = Icons.Outlined.WaterDrop,
                                tint = HumidityBlue,
                                text = "Humedad: ${HuertoRepository.interpretHumidity(humidity)}"
                            )
                            InterpretationRow(
                                icon = Icons.Outlined.WbSunny,
                                tint = SunYellow,
                                text = "Luz: ${HuertoRepository.interpretLight(sunPercentage, previewHoursText)}"
                            )
                            InterpretationRow(
                                icon = Icons.Outlined.Science,
                                tint = NutrientGreen,
                                text = "NPK ($previewNpkLabel): ${HuertoRepository.interpretNPK(previewNpk)}"
                            )
                        }
                    }

                    if (feedbackBanner.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GreenPrimary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = TextOnDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feedbackBanner,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextOnDark
                                )
                            }
                        }
                    }
                }

                // Botones de acción inferiores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, GreenPrimary)
                    ) {
                        Text("Cerrar", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (currentPlant != null) {
                        Button(
                            onClick = {
                                HuertoRepository.updatePlantSensorReadings(
                                    plantId = currentPlant.id,
                                    humidity = humidity,
                                    sunPercentage = sunPercentage,
                                    n = nitrogen,
                                    p = phosphorus,
                                    k = potassium,
                                    tempCelsius = temperature
                                )
                                HuertoRepository.selectPlant(currentPlant.id)
                                feedbackBanner = "Lecturas guardadas en ${currentPlant.name}. Tablero y alertas actualizados."
                            },
                            modifier = Modifier
                                .weight(1.6f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = TextOnDark
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sensors,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Aplicar lecturas", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InterpretationRow(
    icon: ImageVector,
    tint: Color,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextPrimary,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun QuickScenarioChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

@Composable
private fun SensorSliderRow(
    title: String,
    valueText: String,
    rawText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = rawText,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = activeColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = valueText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = activeColor.copy(alpha = 0.2f)
            )
        )
    }
}
