package com.example.telehuerto.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.PlantIconType
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.PlantPotIllustration
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AddSoftGreen = Color(0xFFDFEDE2)
private val AddPhotoTint = Color(0xFFEAF2E6)
private val AddBorder = Color(0xFFD9DFD3)
private val AddDashed = Color(0xFF90B384)

private data class SensorChannelOption(val label: String)

private val SensorChannels = listOf(
    SensorChannelOption("Canal A4 · Sonda Capacitiva + NPK"),
    SensorChannelOption("Canal A5 · Sonda Humedad + LDR"),
    SensorChannelOption("Canal I2C #2 · Multisensor Ambiental")
)

private val IconChoices = listOf(
    "Tomate" to PlantIconType.TOMATE,
    "Albahaca" to PlantIconType.ALBAHACA,
    "Pimiento" to PlantIconType.PIMIENTO,
    "Brote aromático" to PlantIconType.GENERIC
)

private val LocationChoices = listOf("Balcón", "Interior", "Jardín")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(navController: NavController) {
    var currentStep by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Balcón") }
    var iconType by remember { mutableStateOf(PlantIconType.GENERIC) }
    var photoPicked by remember { mutableStateOf(false) }
    var showStylePicker by remember { mutableStateOf(false) }
    var channel by remember { mutableStateOf(SensorChannels[0].label) }
    var testing by remember { mutableStateOf(false) }
    var testDone by remember { mutableStateOf(false) }

    BackHandler(enabled = currentStep == 2) { currentStep = 1 }

    LaunchedEffect(testing) {
        if (testing) {
            delay(1200)
            testing = false
            testDone = true
        }
    }

    Scaffold(
        containerColor = BackgroundBeige,
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .clickable { if (currentStep == 2) currentStep = 1 else navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Volver", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text("NUEVA PLANTA", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = TextSecondary)
                    Text("Añadir planta", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Stepper(currentStep)

            if (currentStep == 1) {
                // Photo card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AddPhotoTint, RoundedCornerShape(24.dp))
                        .drawBehind {
                            drawRoundRect(
                                color = AddDashed,
                                cornerRadius = CornerRadius(24.dp.toPx()),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                                )
                            )
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoPicked) {
                        PlantPotIllustration(iconType = iconType, modifier = Modifier.size(64.dp))
                    } else {
                        Box(
                            modifier = Modifier.size(64.dp).background(AddSoftGreen, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Añade una foto", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Te ayudará a reconocerla fácilmente.", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            onClick = { showStylePicker = !showStylePicker },
                            shape = RoundedCornerShape(50),
                            color = CardBackground,
                            border = BorderStroke(1.dp, AddBorder)
                        ) {
                            Text(
                                "Elegir foto",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GreenPrimary
                            )
                        }
                    }
                }
                if (showStylePicker) {
                    Surface(shape = RoundedCornerShape(20.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Elige cómo se verá tu planta", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                IconChoices.forEach { (label, type) ->
                                    val selected = photoPicked && iconType == type
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(
                                                if (selected) 2.dp else 1.dp,
                                                if (selected) GreenPrimary else AddBorder,
                                                RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                iconType = type
                                                photoPicked = true
                                                showStylePicker = false
                                            }
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        PlantPotIllustration(iconType = type, modifier = Modifier.size(48.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text(label, fontSize = 10.sp, color = TextPrimary, textAlign = TextAlign.Center, maxLines = 2)
                                    }
                                }
                            }
                        }
                    }
                }

                // Info card
                Surface(shape = RoundedCornerShape(24.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Información de la planta", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        FormField("Nombre de la planta", name, "Ej. Menta del balcón") { name = it }
                        FormField("Especie o variedad", species, "Ej. Mentha spicata") { species = it }
                        FormField(
                            label = "Fecha de plantación",
                            value = date,
                            placeholder = "dd/mm/aaaa",
                            trailing = {
                                IconButton(onClick = {
                                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                                }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Usar fecha de hoy", tint = GreenPrimary)
                                }
                            }
                        ) { date = it }

                        Text("Ubicación", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            LocationOption("Balcón", Icons.Default.WbSunny, location == "Balcón", Modifier.weight(1f)) { location = "Balcón" }
                            LocationOption("Interior", Icons.Default.Home, location == "Interior", Modifier.weight(1f)) { location = "Interior" }
                            LocationOption("Jardín", Icons.Default.Eco, location == "Jardín", Modifier.weight(1f)) { location = "Jardín" }
                        }
                        if (name.isBlank()) {
                            Text("Ponle un nombre para continuar.", fontSize = 12.sp, color = TextSecondary)
                        }
                        Button(
                            onClick = { currentStep = 2 },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White)
                        ) {
                            Text("Continuar a vinculación de sensor  >", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                val device = HuertoRepository.arduinoDevice
                // Device explanation
                Surface(shape = RoundedCornerShape(24.dp), color = AddSoftGreen, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Vincula tu planta con el Arduino", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text("🔗 ${device.model} · #${device.id}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Cada planta usa su propio canal del dispositivo. Así las lecturas de humedad, luz y nutrientes de \"${name.ifBlank { "tu planta" }}\" nunca se mezclan con las de otra planta u otro huerto.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(24.dp), color = CardBackground, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Canal de sensor Arduino", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        SensorChannels.forEach { option ->
                            val selected = channel == option.label
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (selected) AddSoftGreen else CardBackground, RoundedCornerShape(16.dp))
                                    .border(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) GreenPrimary else AddBorder,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { channel = option.label; testDone = false }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(option.label, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                if (selected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { testDone = false; testing = true },
                            enabled = !testing,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, GreenPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                        ) {
                            Text(if (testing) "Leyendo sensor…" else "Probar lectura del sensor", fontWeight = FontWeight.SemiBold)
                        }
                        if (testDone) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AddSoftGreen, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Text("Humedad 68% · Luz Media · NPK 75%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Lectura traducida: Condiciones iniciales óptimas para trasplante",
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { currentStep = 1 },
                        modifier = Modifier.weight(0.7f).height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, AddBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("< Volver", fontWeight = FontWeight.SemiBold, maxLines = 1) }
                    Button(
                        onClick = {
                            HuertoRepository.addPlant(
                                name = name.trim(),
                                species = species.trim().ifEmpty { "Especie sin definir" },
                                date = date.trim().ifEmpty { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) },
                                location = location,
                                sensorChannel = channel,
                                iconType = iconType
                            )
                            navController.navigate("plants") {
                                popUpTo("plants") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1.3f).height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("Registrar planta en el huerto", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, textAlign = TextAlign.Center) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Stepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        StepCircle(1, "Información", currentStep >= 1, currentStep == 1)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 17.dp)
                .height(2.dp)
                .background(if (currentStep >= 2) GreenPrimary else AddBorder)
        )
        StepCircle(2, "Sensor", currentStep >= 2, currentStep == 2)
    }
}

@Composable
private fun StepCircle(number: Int, label: String, reached: Boolean, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (reached) GreenPrimary else CardBackground, CircleShape)
                .border(1.5.dp, if (reached) GreenPrimary else AddBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                fontWeight = FontWeight.Bold,
                color = if (reached) Color.White else TextSecondary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    placeholder: String,
    trailing: (@Composable () -> Unit)? = null,
    onChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = TextSecondary) },
            trailingIcon = trailing,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = AddBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}

@Composable
private fun LocationOption(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .background(if (selected) AddSoftGreen else CardBackground, RoundedCornerShape(16.dp))
            .border(if (selected) 2.dp else 1.dp, if (selected) GreenPrimary else AddBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) GreenPrimary else TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selected) GreenPrimary else TextPrimary)
    }
}
