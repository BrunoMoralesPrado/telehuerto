package com.example.telehuerto.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.data.PlantIconType
import com.example.telehuerto.ui.components.BottomNavBar
import com.example.telehuerto.ui.components.MediaSourceSheetDialog
import com.example.telehuerto.ui.components.PhotoStorageHelper
import com.example.telehuerto.ui.components.PlantAvatar
import com.example.telehuerto.ui.components.VectorIllustrationPickerDialog
import com.example.telehuerto.ui.theme.AlertCritical
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.BorderSubtle
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.GreenDark
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.GreenSoft
import com.example.telehuerto.ui.theme.StatusAttentionBg
import com.example.telehuerto.ui.theme.SurfaceTinted
import com.example.telehuerto.ui.theme.TextOnDark
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import com.example.telehuerto.util.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val FALLBACK_SENSOR_CHANNEL = "Canal A1 (Sonda Suelo + Luz)"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    navController: NavController,
    editPlantId: String? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val editingPlant = remember(editPlantId) {
        editPlantId?.let { HuertoRepository.getPlantById(it) }
    }
    val isEditMode = editingPlant != null

    val availableChannels = HuertoRepository.availableSensorChannels(excludePlantId = editPlantId)

    var currentStep by rememberSaveable(editPlantId) { mutableIntStateOf(1) }
    var plantName by rememberSaveable(editPlantId) { mutableStateOf(editingPlant?.name ?: "") }
    var species by rememberSaveable(editPlantId) { mutableStateOf(editingPlant?.species ?: "") }
    var plantedDate by rememberSaveable(editPlantId) { mutableStateOf(editingPlant?.plantedDate ?: "") }
    var location by rememberSaveable(editPlantId) { mutableStateOf(editingPlant?.location ?: "Balcón") }
    var selectedIconTypeName by rememberSaveable(editPlantId) {
        mutableStateOf((editingPlant?.iconType ?: PlantIconType.GENERIC).name)
    }
    var photoUri by rememberSaveable(editPlantId) { mutableStateOf(editingPlant?.photoUri) }
    var selectedChannel by rememberSaveable(editPlantId) {
        mutableStateOf(
            editingPlant?.sensorChannel
                ?: availableChannels.firstOrNull()
                ?: FALLBACK_SENSOR_CHANNEL
        )
    }

    var sensorTested by rememberSaveable(editPlantId) { mutableStateOf(false) }
    var isTestingSensor by remember { mutableStateOf(false) }
    var testRunCount by rememberSaveable(editPlantId) { mutableIntStateOf(0) }
    var testHumidity by rememberSaveable(editPlantId) { mutableIntStateOf(editingPlant?.humidity ?: 68) }
    var testSun by rememberSaveable(editPlantId) { mutableIntStateOf(editingPlant?.sunPercentage ?: 72) }
    var testN by rememberSaveable(editPlantId) { mutableIntStateOf(editingPlant?.npk?.n ?: 72) }
    var testP by rememberSaveable(editPlantId) { mutableIntStateOf(editingPlant?.npk?.p ?: 68) }
    var testK by rememberSaveable(editPlantId) { mutableIntStateOf(editingPlant?.npk?.k ?: 74) }

    var nameError by rememberSaveable(editPlantId) { mutableStateOf<String?>(null) }
    var speciesError by rememberSaveable(editPlantId) { mutableStateOf<String?>(null) }
    var dateError by rememberSaveable(editPlantId) { mutableStateOf<String?>(null) }

    var showMediaSheet by rememberSaveable(editPlantId) { mutableStateOf(false) }
    var showVectorPicker by rememberSaveable(editPlantId) { mutableStateOf(false) }
    var showDatePicker by rememberSaveable(editPlantId) { mutableStateOf(false) }
    var pendingCameraPath by rememberSaveable(editPlantId) { mutableStateOf<String?>(null) }

    val selectedIconType = remember(selectedIconTypeName) {
        PlantIconType.entries.firstOrNull { it.name == selectedIconTypeName } ?: PlantIconType.GENERIC
    }

    LaunchedEffect(availableChannels) {
        if (availableChannels.isNotEmpty() && selectedChannel !in availableChannels) {
            selectedChannel = availableChannels.first()
        }
    }

    LaunchedEffect(isTestingSensor) {
        if (isTestingSensor) {
            delay(750)
            if (!isEditMode) {
                val offsets = listOf(0, 3, -4, 5, -2)
                val delta = offsets[testRunCount % offsets.size]
                testHumidity = (68 + delta).coerceIn(55, 80)
                testSun = (72 - delta / 2).coerceIn(58, 84)
                testN = (72 + delta).coerceIn(60, 84)
                testP = (68 + delta / 2).coerceIn(58, 80)
                testK = (74 - delta).coerceIn(62, 86)
                testRunCount += 1
            }
            isTestingSensor = false
            sensorTested = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val savedPath = withContext(Dispatchers.IO) {
                    PhotoStorageHelper.copyUriToInternalStorage(context, uri)
                }
                if (savedPath != null) {
                    val previous = photoUri
                    photoUri = savedPath
                    if (previous != null && previous != editingPlant?.photoUri) {
                        withContext(Dispatchers.IO) {
                            PhotoStorageHelper.deletePhotoIfManaged(previous)
                        }
                    }
                } else {
                    snackbarHostState.showSnackbar("No pudimos copiar esa foto. Intenta con otra imagen.")
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val capturedPath = pendingCameraPath
        pendingCameraPath = null
        if (success && !capturedPath.isNullOrBlank()) {
            val previous = photoUri
            photoUri = capturedPath
            if (previous != null && previous != editingPlant?.photoUri && previous != capturedPath) {
                scope.launch(Dispatchers.IO) {
                    PhotoStorageHelper.deletePhotoIfManaged(previous)
                }
            }
        } else if (!capturedPath.isNullOrBlank()) {
            scope.launch(Dispatchers.IO) {
                PhotoStorageHelper.deletePhotoIfManaged(capturedPath)
            }
        }
    }

    fun launchCamera() {
        try {
            val (uri, file) = PhotoStorageHelper.createCameraPhotoTarget(context)
            pendingCameraPath = file.absolutePath
            cameraLauncher.launch(uri)
        } catch (_: Throwable) {
            pendingCameraPath = null
            scope.launch {
                snackbarHostState.showSnackbar("No pudimos abrir la cámara en este dispositivo.")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Permiso de cámara denegado. Puedes elegir de galería o usar ilustraciones."
                )
            }
        }
    }

    fun requestCameraOrLaunch() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun validateStep1(): Boolean {
        val nameResult = Validators.validateName(plantName)
        val speciesResult = Validators.validateSpecies(species)
        val dateResult = Validators.validateDateDdMmYyyy(plantedDate)
        nameError = nameResult.errorMessage
        speciesError = speciesResult.errorMessage
        dateError = dateResult.errorMessage
        return nameResult.isValid && speciesResult.isValid && dateResult.isValid
    }

    BackHandler(enabled = currentStep == 2) {
        currentStep = 1
    }

    Scaffold(
        containerColor = BackgroundBeige,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController, "plants") }
    ) { padding ->
        if (!editPlantId.isNullOrBlank() && editingPlant == null) {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Esta planta ya no está disponible",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                navController.navigate("plants") {
                                    popUpTo("plants") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = TextOnDark
                            )
                        ) {
                            Text("Volver a Mis plantas", fontWeight = FontWeight.SemiBold)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep == 2) currentStep = 1 else navController.popBackStack()
                        },
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = if (isEditMode) "EDITAR PLANTA" else "NUEVA PLANTA",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.4.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isEditMode) "Editar datos" else "Añadir planta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                }
            }

            Stepper(currentStep = currentStep)

            if (currentStep == 1) {
                // Vista previa de imagen / ilustración
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlantAvatar(
                            iconType = selectedIconType,
                            photoUri = photoUri,
                            modifier = Modifier.size(84.dp),
                            cornerRadius = 18.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (!photoUri.isNullOrBlank()) {
                                    "Fotografía seleccionada"
                                } else {
                                    selectedIconType.displayName
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Sube una foto real o elige una de las 14 ilustraciones botánicas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = { showMediaSheet = true },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.45f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!photoUri.isNullOrBlank() || isEditMode) {
                                        "Cambiar imagen"
                                    } else {
                                        "Elegir imagen"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Formulario de datos principales
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información de la planta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ValidatedFormField(
                            label = "Nombre de la planta",
                            value = plantName,
                            placeholder = "Ej. Menta del balcón",
                            errorMessage = nameError,
                            onChange = {
                                plantName = it
                                if (nameError != null) {
                                    nameError = Validators.validateName(it).errorMessage
                                }
                            }
                        )

                        ValidatedFormField(
                            label = "Especie o variedad",
                            value = species,
                            placeholder = "Ej. Mentha spicata",
                            errorMessage = speciesError,
                            onChange = {
                                species = it
                                if (speciesError != null) {
                                    speciesError = Validators.validateSpecies(it).errorMessage
                                }
                            }
                        )

                        ValidatedFormField(
                            label = "Fecha de plantación",
                            value = plantedDate,
                            placeholder = "dd/MM/yyyy",
                            errorMessage = dateError,
                            trailing = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = "Elegir fecha en calendario",
                                        tint = GreenPrimary
                                    )
                                }
                            },
                            onChange = {
                                plantedDate = it
                                if (dateError != null) {
                                    dateError = Validators.validateDateDdMmYyyy(it).errorMessage
                                }
                            }
                        )

                        Text(
                            text = "Ubicación",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LocationOption(
                                label = "Balcón",
                                icon = Icons.Outlined.WbSunny,
                                selected = location == "Balcón",
                                modifier = Modifier.weight(1f),
                                onClick = { location = "Balcón" }
                            )
                            LocationOption(
                                label = "Interior",
                                icon = Icons.Outlined.Home,
                                selected = location == "Interior",
                                modifier = Modifier.weight(1f),
                                onClick = { location = "Interior" }
                            )
                            LocationOption(
                                label = "Jardín",
                                icon = Icons.Outlined.Eco,
                                selected = location == "Jardín",
                                modifier = Modifier.weight(1f),
                                onClick = { location = "Jardín" }
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Button(
                            onClick = {
                                if (validateStep1()) {
                                    currentStep = 2
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = TextOnDark
                            )
                        ) {
                            Text(
                                text = "Continuar a sensor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // Paso 2: Vinculación de sensor
                val device = HuertoRepository.arduinoDevice

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = GreenSoft,
                    border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Vincula tu planta con el Arduino",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenDark
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${device.model} · ${device.id}",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Cada planta activa usa su propio canal para que las lecturas de \"${plantName.trim().ifBlank { "tu planta" }}\" no se mezclen con las demás.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

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
                            text = "Canal disponible",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        if (availableChannels.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = StatusAttentionBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Todos los canales están ocupados por plantas activas. Se asignará el canal predeterminado hasta que liberes uno.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            availableChannels.forEach { channelOption ->
                                val isSelected = selectedChannel == channelOption
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) GreenSoft.copy(alpha = 0.65f) else SurfaceTinted)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) GreenPrimary else BorderSubtle,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            selectedChannel = channelOption
                                            sensorTested = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = channelOption,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Canal seleccionado",
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedButton(
                            onClick = {
                                sensorTested = false
                                isTestingSensor = true
                            },
                            enabled = !isTestingSensor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GreenPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sensors,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTestingSensor) "Leyendo sensor..." else "Probar lectura del sensor",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (sensorTested) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GreenSoft.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Humedad $testHumidity% · Luz ${HuertoRepository.lightLevelFor(testSun)} ($testSun%) · NPK $testN/$testP/$testK",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenDark
                                    )
                                    Text(
                                        text = HuertoRepository.interpretHumidity(testHumidity),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Acciones finales
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { currentStep = 1 },
                        modifier = Modifier
                            .weight(0.7f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Volver",
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = {
                            if (!validateStep1()) {
                                currentStep = 1
                                return@Button
                            }
                            if (editingPlant == null) {
                                HuertoRepository.addPlant(
                                    name = plantName.trim(),
                                    species = species.trim(),
                                    date = plantedDate.trim(),
                                    location = location,
                                    sensorChannel = selectedChannel,
                                    iconType = selectedIconType,
                                    photoUri = photoUri,
                                    initialHumidity = testHumidity,
                                    initialSun = testSun,
                                    initialN = testN,
                                    initialP = testP,
                                    initialK = testK
                                )
                                navController.navigate("plants") {
                                    popUpTo("plants") { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                HuertoRepository.updatePlantDetails(
                                    plantId = editingPlant.id,
                                    name = plantName.trim(),
                                    species = species.trim(),
                                    date = plantedDate.trim(),
                                    location = location,
                                    sensorChannel = selectedChannel,
                                    iconType = selectedIconType,
                                    photoUri = photoUri
                                )
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = TextOnDark
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (isEditMode) "Guardar cambios" else "Registrar planta",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        if (showMediaSheet) {
            MediaSourceSheetDialog(
                hasCurrentPhoto = !photoUri.isNullOrBlank(),
                onPickGallery = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTakeCamera = { requestCameraOrLaunch() },
                onPickVector = { showVectorPicker = true },
                onClearPhoto = {
                    val previous = photoUri
                    photoUri = null
                    if (previous != null && previous != editingPlant?.photoUri) {
                        scope.launch(Dispatchers.IO) {
                            PhotoStorageHelper.deletePhotoIfManaged(previous)
                        }
                    }
                },
                onDismiss = { showMediaSheet = false }
            )
        }

        if (showVectorPicker) {
            VectorIllustrationPickerDialog(
                selectedType = selectedIconType,
                onSelect = { chosen ->
                    selectedIconTypeName = chosen.name
                    val previous = photoUri
                    photoUri = null
                    if (previous != null && previous != editingPlant?.photoUri) {
                        scope.launch(Dispatchers.IO) {
                            PhotoStorageHelper.deletePhotoIfManaged(previous)
                        }
                    }
                    if (species.isBlank() && chosen != PlantIconType.GENERIC) {
                        species = chosen.defaultSpecies
                        speciesError = null
                    }
                    showVectorPicker = false
                },
                onDismiss = { showVectorPicker = false }
            )
        }

        if (showDatePicker) {
            val initialMillis = remember(plantedDate) {
                parseDdMmYyyyToUtcMillis(plantedDate)
            }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialMillis
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                colors = DatePickerDefaults.colors(containerColor = CardBackground),
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedUtcMillis ->
                                val formatted = formatUtcMillisToDdMmYyyy(selectedUtcMillis)
                                plantedDate = formatted
                                dateError = Validators.validateDateDdMmYyyy(formatted).errorMessage
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(
                            text = "Aceptar",
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = "Cancelar", color = TextSecondary)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = CardBackground,
                        selectedDayContainerColor = GreenPrimary,
                        todayContentColor = GreenPrimary,
                        todayDateBorderColor = GreenPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun Stepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        StepCircle(
            number = 1,
            label = "Datos",
            reached = currentStep >= 1,
            active = currentStep == 1
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 17.dp)
                .height(2.dp)
                .background(if (currentStep >= 2) GreenPrimary else BorderSubtle)
        )
        StepCircle(
            number = 2,
            label = "Sensor",
            reached = currentStep >= 2,
            active = currentStep == 2
        )
    }
}

@Composable
private fun StepCircle(
    number: Int,
    label: String,
    reached: Boolean,
    active: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(if (reached) GreenPrimary else CardBackground, CircleShape)
                .border(1.5.dp, if (reached) GreenPrimary else BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (reached) TextOnDark else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun ValidatedFormField(
    label: String,
    value: String,
    placeholder: String,
    errorMessage: String?,
    trailing: (@Composable () -> Unit)? = null,
    onChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            trailingIcon = trailing,
            isError = errorMessage != null,
            supportingText = if (errorMessage != null) {
                {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = AlertCritical
                    )
                }
            } else {
                null
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderSubtle,
                errorBorderColor = AlertCritical,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}

@Composable
private fun LocationOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) GreenSoft.copy(alpha = 0.7f) else SurfaceTinted)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) GreenPrimary else BorderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) GreenPrimary else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) GreenDark else TextPrimary
        )
    }
}

private fun formatUtcMillisToDdMmYyyy(utcMillis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(utcMillis))
}

private fun parseDdMmYyyyToUtcMillis(dateText: String): Long? {
    val trimmed = dateText.trim()
    if (trimmed.isEmpty()) return null
    return try {
        val parser = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        parser.parse(trimmed)?.time
    } catch (_: Throwable) {
        null
    }
}
