package com.example.telehuerto.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.NPKValues
import com.example.telehuerto.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun ArduinoSimDialog(onDismiss: () -> Unit) {
    val activePlants = HuertoRepository.activePlants()
    val device = HuertoRepository.arduinoDevice

    var selectedPlantId by remember {
        mutableStateOf(
            activePlants.firstOrNull { it.id == HuertoRepository.selectedPlantId }?.id
                ?: activePlants.firstOrNull()?.id
                ?: "albahaca"
        )
    }

    val currentPlant = HuertoRepository.getPlantById(selectedPlantId) ?: activePlants.firstOrNull()

    var humidity by remember(selectedPlantId) { mutableIntStateOf(currentPlant?.humidity ?: 40) }
    var sunPercentage by remember(selectedPlantId) { mutableIntStateOf(currentPlant?.sunPercentage ?: 66) }
    var nitrogen by remember(selectedPlantId) { mutableIntStateOf(currentPlant?.npk?.n ?: 24) }
    var phosphorus by remember(selectedPlantId) { mutableIntStateOf(currentPlant?.npk?.p ?: 48) }
    var potassium by remember(selectedPlantId) { mutableIntStateOf(currentPlant?.npk?.k ?: 61) }
    var temperature by remember { mutableIntStateOf(device.temperatureCelsius) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }

    val previewNpk = NPKValues(
        n = nitrogen,
        p = phosphorus,
        k = potassium,
        statusLabel = if (nitrogen < 35 || phosphorus < 35 || potassium < 35) "Bajos" else "Óptimos"
    )
    val previewHoursText = "${(sunPercentage * 0.075f).roundToInt()} h 30 min de sol"
    val willNeedAttention = humidity < 45 || nitrogen < 35 || sunPercentage < 40

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundBeige),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = GreenPrimary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = "Arduino IoT",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "NODO IOT & TELEMETRÍA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Simulador Arduino",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: User Authentication & Physical Arduino Pairing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IDENTIDAD Y VINCULACIÓN FÍSICA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE2EDE0)
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
                                        text = "WiFi ${device.wifiSignalPercent}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${device.model} · Serie: ${device.id}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Propietario autenticado: ${HuertoRepository.currentUserName} (${HuertoRepository.currentUserEmail})",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "MAC: A4:CF:12:8B:4E:01 · Sincronizado ${device.lastSyncText}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pin Mapping badges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PinBadge("A0", "Humedad Suelo")
                            PinBadge("A1", "LDR Luz Solar")
                            PinBadge("A2", "Sonda NPK RS485")
                            PinBadge("A3", "DHT22 Temp")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundBeige,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🔒 Privacidad de datos: Cada placa Arduino incluye su ID único (${device.id}) y token criptográfico asociado a ${device.assignedUserEmail}. Así el servidor distingue este huerto y bloquea el acceso a terceros.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Plant selector & Quick Scenarios
                Text(
                    text = "1. SELECCIONA LA PLANTA A SIMULAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activePlants.forEach { plant ->
                        val isSelected = plant.id == selectedPlantId
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) GreenPrimary else CardBackground,
                            border = if (isSelected) null else BorderStroke(1.dp, GreenSecondary.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                selectedPlantId = plant.id
                                feedbackBanner = null
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else GreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = plant.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                    Text(
                                        text = plant.sensorChannel,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "2. ESCENARIOS RÁPIDOS DE PRUEBA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickScenarioChip(
                        label = "🔥 Sequía Crítica (18%)",
                        tint = Color(0xFFB55B52),
                        bg = Color(0xFFF7E1DC)
                    ) {
                        humidity = 18
                        sunPercentage = 88
                        nitrogen = 38
                        temperature = 30
                        feedbackBanner = null
                    }
                    QuickScenarioChip(
                        label = "💧 Riego Óptimo (78%)",
                        tint = Color(0xFF2B7A88),
                        bg = Color(0xFFDFEFF2)
                    ) {
                        humidity = 78
                        sunPercentage = 76
                        nitrogen = 74
                        phosphorus = 70
                        potassium = 78
                        temperature = 24
                        feedbackBanner = null
                    }
                    QuickScenarioChip(
                        label = "🌱 Déficit Nutrientes (N 20%)",
                        tint = Color(0xFFB8862D),
                        bg = Color(0xFFF9EDD4)
                    ) {
                        humidity = 62
                        sunPercentage = 66
                        nitrogen = 20
                        phosphorus = 32
                        potassium = 40
                        temperature = 24
                        feedbackBanner = null
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Sliders for raw sensor readings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LECTURAS DEL ARDUINO EN TIEMPO REAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Humidity Slider
                        SensorSliderRow(
                            title = "Pin A0 · Humedad del Suelo",
                            valueText = "$humidity%",
                            rawText = "ADC: ${(1023 - humidity * 6.2f).roundToInt()} raw",
                            value = humidity.toFloat(),
                            valueRange = 5f..98f,
                            activeColor = Color(0xFF2B7A88),
                            onValueChange = { humidity = it.roundToInt() }
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BackgroundBeige)

                        // Sunlight Slider
                        SensorSliderRow(
                            title = "Pin A1 · Iluminación Solar (LDR)",
                            valueText = "$sunPercentage%",
                            rawText = "${sunPercentage * 45} lux · $previewHoursText",
                            value = sunPercentage.toFloat(),
                            valueRange = 10f..100f,
                            activeColor = Color(0xFFB8862D),
                            onValueChange = { sunPercentage = it.roundToInt() }
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BackgroundBeige)

                        // Nitrogen NPK Slider
                        SensorSliderRow(
                            title = "Pin A2 · Nitrógeno y Nutrientes (NPK)",
                            valueText = "N $nitrogen%",
                            rawText = "P: $phosphorus% · K: $potassium%",
                            value = nitrogen.toFloat(),
                            valueRange = 10f..95f,
                            activeColor = Color(0xFFB55B52),
                            onValueChange = {
                                nitrogen = it.roundToInt()
                                phosphorus = (nitrogen + 12).coerceAtMost(95)
                                potassium = (nitrogen + 18).coerceAtMost(95)
                            }
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = BackgroundBeige)

                        // Temperature Slider
                        SensorSliderRow(
                            title = "Pin A3 · Temperatura Ambiental (DHT22)",
                            valueText = "$temperature°C",
                            rawText = if (temperature > 28) "Ambiente cálido" else "Rango ideal (18–26°C)",
                            value = temperature.toFloat(),
                            valueRange = 12f..38f,
                            activeColor = GreenPrimary,
                            onValueChange = { temperature = it.roundToInt() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Real-time Interpretation Preview (How data turns into decisions)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (willNeedAttention) Color(0xFFF9E3DC) else Color(0xFFE2EDE0)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRADUCCIÓN A DECISIONES DEL USUARIO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (willNeedAttention) Color(0xFFB55B52) else GreenPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = if (willNeedAttention) "Requiere atención" else "Saludable",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (willNeedAttention) Color(0xFFB55B52) else GreenPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "💧 Humedad: ${HuertoRepository.interpretHumidity(humidity)}",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "☀️ Luz: ${HuertoRepository.interpretLight(sunPercentage, previewHoursText)}",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🧪 NPK: ${HuertoRepository.interpretNPK(previewNpk)}",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (feedbackBanner != null) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackBanner!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
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

                    Button(
                        onClick = {
                            if (currentPlant != null) {
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
                                feedbackBanner = "Lecturas enviadas a ${currentPlant.name}. Interfaz y alertas actualizadas."
                            }
                        },
                        modifier = Modifier
                            .weight(1.6f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aplicar al Huerto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PinBadge(pin: String, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFE2EDE0)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pin,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GreenPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun QuickScenarioChip(
    label: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
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
            Column {
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
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = activeColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = valueText,
                    fontSize = 13.sp,
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
