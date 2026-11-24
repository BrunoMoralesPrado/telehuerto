package com.example.telehuerto.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.telehuerto.data.local.AlertDao
import com.example.telehuerto.data.local.LogDao
import com.example.telehuerto.data.local.NaturaDbHelper
import com.example.telehuerto.data.local.PlantDao
import com.example.telehuerto.data.local.UserDao
import com.example.telehuerto.data.local.WeatherSyncDao
import com.example.telehuerto.data.remote.ApiConfig
import com.example.telehuerto.data.remote.CloudSyncVolleyService
import com.example.telehuerto.data.remote.VolleySingleton
import com.example.telehuerto.data.remote.WeatherVolleyService
import com.example.telehuerto.util.Validators
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Fuente única de datos de Natura.
 *
 * - SQLite (NaturaDbHelper + DAOs) es la fuente persistente. Todas las lecturas y
 *   escrituras se hacen en [Dispatchers.IO] (serializadas para respetar el orden).
 * - El estado de Compose (mutableStateOf / mutableStateListOf) solo se modifica en
 *   el hilo principal: desde la UI, desde callbacks de Volley o al volver de IO.
 * - Volley respalda plantas y bitácora (POST / PUT / DELETE) y consulta el clima (GET).
 */
object HuertoRepository {

    private const val TAG = "HuertoRepository"

    // Umbrales de salud
    const val HUMIDITY_MIN = 35
    const val NITROGEN_MIN = 30
    const val SUN_MIN = 40

    private const val DEMO_EMAIL = "andrea@huerto.com"
    private const val DEMO_PASSWORD = "huerto2026"
    private const val MAX_WATERING_HUMIDITY = 85
    private const val SYNC_HISTORY_LIMIT = 50

    private val SENSOR_CHANNEL_POOL = listOf(
        "Canal A1 (Sonda Suelo + Luz)",
        "Canal A2 (Sonda NPK + Humedad)",
        "Canal A3 (Sonda Multiparámetro)",
        "Canal A4 (Sonda Capacitiva + NPK)",
        "Canal A5 (Sonda Humedad + LDR)",
        "Canal I2C #1 (Sensor Ambiental)",
        "Canal I2C #2 (Multisensor Suelo)",
        "Canal D2 (Sonda Riego)"
    )

    /** Coordenadas conocidas para evitar geocodificar ciudades frecuentes. */
    private val KNOWN_CITIES = mapOf(
        "ciudad de mexico" to (19.4326 to -99.1332),
        "cdmx" to (19.4326 to -99.1332),
        "guadalajara" to (20.6597 to -103.3496),
        "monterrey" to (25.6866 to -100.3161),
        "puebla" to (19.0414 to -98.2063),
        "merida" to (20.9674 to -89.5926),
        "queretaro" to (20.5888 to -100.3899),
        "oaxaca" to (17.0732 to -96.7266),
        "madrid" to (40.4168 to -3.7038),
        "barcelona" to (41.3874 to 2.1686),
        "bogota" to (4.7110 to -74.0721),
        "lima" to (-12.0464 to -77.0428),
        "buenos aires" to (-34.6037 to -58.3816),
        "santiago" to (-33.4489 to -70.6693)
    )

    // ---------------------------------------------------------------------
    // Infraestructura
    // ---------------------------------------------------------------------
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** IO serializado: las escrituras se aplican en el mismo orden en que se piden. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dbDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    private var appContext: Context? = null
    private var dbHelper: NaturaDbHelper? = null
    private var userDao: UserDao? = null
    private var plantDao: PlantDao? = null
    private var alertDao: AlertDao? = null
    private var logDao: LogDao? = null
    private var weatherSyncDao: WeatherSyncDao? = null
    private var initStarted = false
    private var weatherRequestToken = 0

    // ---------------------------------------------------------------------
    // Estado de sesión y dispositivo
    // ---------------------------------------------------------------------
    var isInitialized by mutableStateOf(false)
    var isLoggedIn by mutableStateOf(false)
    var currentUser by mutableStateOf<UserAccount?>(null)
    var currentUserName by mutableStateOf("Andrea")
    var currentUserEmail by mutableStateOf(DEMO_EMAIL)
    var currentCity by mutableStateOf("Ciudad de México")
    var selectedPlantId by mutableStateOf("albahaca")
    var nextWateringText by mutableStateOf("Mañana a las 8:00")
    var arduinoDevice by mutableStateOf(
        ArduinoDevice(
            id = "ARD-01",
            name = "Nodo Huerto Balcón",
            model = "Arduino UNO R4 WiFi",
            isConnected = true,
            lastSyncText = "hace 2 min",
            temperatureCelsius = 24,
            wifiSignalPercent = 96,
            assignedUserEmail = DEMO_EMAIL
        )
    )

    val plants = mutableStateListOf<Plant>()
    val alerts = mutableStateListOf<HuertoAlert>()
    val logEntries = mutableStateListOf<LogEntry>()

    // ---------------------------------------------------------------------
    // Estado de red
    // ---------------------------------------------------------------------
    var weatherState by mutableStateOf<NetworkUiState<WeatherSnapshot>>(NetworkUiState.Idle)
    var citySearchState by mutableStateOf<NetworkUiState<List<CityOption>>>(NetworkUiState.Idle)
    var isSyncingCloud by mutableStateOf(false)
    var lastSyncMessage by mutableStateOf<String?>(null)
    val syncHistory = mutableStateListOf<SyncRecord>()

    // ---------------------------------------------------------------------
    // Inicialización
    // ---------------------------------------------------------------------

    /** Idempotente: puede llamarse desde Application y desde MainActivity. */
    fun init(context: Context) {
        synchronized(this) {
            if (initStarted) return
            initStarted = true
            val ctx = context.applicationContext ?: context
            appContext = ctx
            val helper = NaturaDbHelper.getInstance(ctx)
            dbHelper = helper
            userDao = UserDao(helper)
            plantDao = PlantDao(helper)
            alertDao = AlertDao(helper)
            logDao = LogDao(helper)
            weatherSyncDao = WeatherSyncDao(helper)
        }

        mainScope.launch {
            val snapshot = try {
                withContext(dbDispatcher) {
                    val users = userDao!!
                    val loggedUser = users.getLoggedInUser()
                    val user = loggedUser ?: users.findByEmail(DEMO_EMAIL)
                    loadSnapshot(user, loggedIn = loggedUser != null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo cargar la base de datos local", e)
                null
            }
            if (snapshot != null) applySnapshot(snapshot)
            isInitialized = true
            if (isLoggedIn) refreshWeather()
        }
    }

    private data class GardenSnapshot(
        val user: UserAccount?,
        val loggedIn: Boolean,
        val plants: List<Plant>,
        val alerts: List<HuertoAlert>,
        val logs: List<LogEntry>,
        val cachedWeather: WeatherSnapshot?,
        val history: List<SyncRecord>
    )

    /** Debe ejecutarse en IO. */
    private fun loadSnapshot(user: UserAccount?, loggedIn: Boolean): GardenSnapshot {
        val userId = user?.id ?: 1L
        val userPlants = plantDao?.getPlantsForUser(userId).orEmpty()
        val plantIds = userPlants.map { it.id }.toSet()
        val userAlerts = alertDao?.getAllAlerts().orEmpty().filter { it.plantId in plantIds }
        val userLogs = logDao?.getAllEntries().orEmpty()
            .filter { it.plantId == null || it.plantId in plantIds }
        val city = user?.city ?: currentCity
        val weather = weatherSyncDao?.let { it.getLatestWeatherForCity(city) ?: it.getLatestAnyWeather() }
        val history = weatherSyncDao?.getSyncHistory(SYNC_HISTORY_LIMIT).orEmpty()
        return GardenSnapshot(
            user = user,
            loggedIn = loggedIn,
            plants = userPlants,
            alerts = userAlerts,
            logs = userLogs,
            cachedWeather = weather?.copy(isFromCache = true),
            history = history
        )
    }

    /** Debe ejecutarse en Main. */
    private fun applySnapshot(snapshot: GardenSnapshot) {
        val user = snapshot.user
        currentUser = if (snapshot.loggedIn) user else null
        isLoggedIn = snapshot.loggedIn
        if (user != null) {
            currentUserName = user.name
            currentUserEmail = user.email
            currentCity = user.city
            arduinoDevice = arduinoDevice.copy(
                id = user.arduinoSerial,
                assignedUserEmail = user.email
            )
        }
        plants.clear()
        plants.addAll(snapshot.plants)
        alerts.clear()
        alerts.addAll(snapshot.alerts)
        logEntries.clear()
        logEntries.addAll(snapshot.logs)
        syncHistory.clear()
        syncHistory.addAll(snapshot.history)
        if (getPlantById(selectedPlantId) == null) {
            selectedPlantId = activePlants().firstOrNull()?.id ?: plants.firstOrNull()?.id ?: ""
        }
        weatherState = snapshot.cachedWeather
            ?.let { NetworkUiState.Success(it) }
            ?: NetworkUiState.Idle
        citySearchState = NetworkUiState.Idle
        lastSyncMessage = null
    }

    // ---------------------------------------------------------------------
    // Autenticación
    // ---------------------------------------------------------------------

    fun login(email: String, password: String, onResult: (Result<UserAccount>) -> Unit) {
        val emailCheck = Validators.validateEmail(email)
        if (!emailCheck.isValid) {
            onResult(failure(emailCheck.errorMessage ?: "Correo no válido."))
            return
        }
        val passwordCheck = Validators.validatePassword(password)
        if (!passwordCheck.isValid) {
            onResult(failure(passwordCheck.errorMessage ?: "Contraseña no válida."))
            return
        }
        val users = userDao
        if (users == null) {
            onResult(failure("La app todavía se está preparando. Intenta de nuevo."))
            return
        }
        mainScope.launch {
            val outcome: Result<GardenSnapshot> = try {
                withContext(dbDispatcher) {
                    val hash = Validators.sha256(password)
                    val user = users.authenticate(email.trim(), hash)
                    if (user == null) {
                        Result.failure(IllegalArgumentException("Correo o contraseña incorrectos."))
                    } else {
                        users.setLoggedIn(user.id, true)
                        Result.success(loadSnapshot(user.copy(isLoggedIn = true), loggedIn = true))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al iniciar sesión", e)
                Result.failure(IllegalStateException("No se pudo iniciar sesión. Intenta de nuevo."))
            }
            outcome.fold(
                onSuccess = { snapshot ->
                    applySnapshot(snapshot)
                    val user = snapshot.user!!
                    onResult(Result.success(user))
                    refreshWeather()
                },
                onFailure = { onResult(Result.failure(it)) }
            )
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        arduinoSerial: String,
        city: String,
        onResult: (Result<UserAccount>) -> Unit
    ) {
        val firstError = listOf(
            Validators.validateName(name),
            Validators.validateEmail(email),
            Validators.validatePassword(password),
            Validators.validateArduinoSerial(arduinoSerial),
            Validators.validateCityName(city)
        ).firstOrNull { !it.isValid }
        if (firstError != null) {
            onResult(failure(firstError.errorMessage ?: "Revisa los datos del registro."))
            return
        }
        val users = userDao
        if (users == null) {
            onResult(failure("La app todavía se está preparando. Intenta de nuevo."))
            return
        }
        mainScope.launch {
            val outcome: Result<GardenSnapshot> = try {
                withContext(dbDispatcher) {
                    if (users.findByEmail(email) != null) {
                        Result.failure(IllegalArgumentException("Ya existe una cuenta con ese correo."))
                    } else {
                        val newUser = UserAccount(
                            name = name.trim(),
                            email = email.trim().lowercase(Locale.US),
                            passwordHash = Validators.sha256(password),
                            arduinoSerial = arduinoSerial.trim(),
                            city = city.trim(),
                            isLoggedIn = true
                        )
                        val id = users.insertUser(newUser)
                        if (id <= 0L) {
                            Result.failure(IllegalStateException("No se pudo crear la cuenta."))
                        } else {
                            users.setLoggedIn(id, true)
                            Result.success(loadSnapshot(newUser.copy(id = id), loggedIn = true))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al registrar", e)
                Result.failure(IllegalStateException("No se pudo crear la cuenta. Intenta de nuevo."))
            }
            outcome.fold(
                onSuccess = { snapshot ->
                    applySnapshot(snapshot)
                    onResult(Result.success(snapshot.user!!))
                    refreshWeather()
                },
                onFailure = { onResult(Result.failure(it)) }
            )
        }
    }

    fun loginDemo(onResult: (Result<UserAccount>) -> Unit) {
        login(DEMO_EMAIL, DEMO_PASSWORD, onResult)
    }

    fun logout() {
        isLoggedIn = false
        currentUser = null
        citySearchState = NetworkUiState.Idle
        db { userDao?.logoutAll() }
    }

    // ---------------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------------
    fun activePlants(): List<Plant> = plants.filter { it.isActive }

    fun retiredPlants(): List<Plant> = plants.filter { !it.isActive }

    fun activeAlerts(): List<HuertoAlert> = alerts.filter { !it.isResolved }

    fun resolvedAlerts(): List<HuertoAlert> = alerts.filter { it.isResolved }

    fun getPlantById(id: String): Plant? = plants.firstOrNull { it.id == id }

    /** Nunca lanza excepción: devuelve null si no hay plantas. */
    fun getSelectedPlantOrNull(): Plant? =
        getPlantById(selectedPlantId)
            ?: activePlants().firstOrNull()
            ?: plants.firstOrNull()

    fun selectPlant(id: String) {
        if (getPlantById(id) != null) selectedPlantId = id
    }

    /**
     * Canales libres del Arduino. Excluye los ocupados por otras plantas activas y
     * siempre conserva el canal propio de [excludePlantId] (útil al editar).
     */
    fun availableSensorChannels(excludePlantId: String? = null): List<String> {
        val occupied = plants
            .filter { it.isActive && it.id != excludePlantId }
            .map { it.sensorChannel }
            .toSet()
        val result = SENSOR_CHANNEL_POOL.filter { it !in occupied }.toMutableList()
        val own = excludePlantId?.let { getPlantById(it)?.sensorChannel }
        if (!own.isNullOrBlank() && own !in result) result.add(0, own)
        return result
    }

    // ---------------------------------------------------------------------
    // CRUD de plantas
    // ---------------------------------------------------------------------
    fun addPlant(
        name: String,
        species: String,
        date: String,
        location: String,
        sensorChannel: String,
        iconType: PlantIconType = PlantIconType.GENERIC,
        photoUri: String? = null,
        initialHumidity: Int = 68,
        initialSun: Int = 72,
        initialN: Int = 72,
        initialP: Int = 68,
        initialK: Int = 74
    ) {
        val cleanName = name.trim().ifBlank { "Nueva planta" }
        val baseId = slugify(cleanName).ifBlank { "planta" }
        var newId = baseId
        while (getPlantById(newId) != null || newId == baseId) {
            // Los ids son globales en SQLite: se agrega un sufijo único.
            newId = "$baseId-${shortId()}"
        }

        val h = initialHumidity.coerceIn(0, 100)
        val sun = initialSun.coerceIn(0, 100)
        val n = initialN.coerceIn(0, 100)
        val p = initialP.coerceIn(0, 100)
        val k = initialK.coerceIn(0, 100)
        val plant = Plant(
            id = newId,
            userId = currentUser?.id ?: 1L,
            name = cleanName,
            species = species.trim().ifBlank { iconType.defaultSpecies },
            plantedDate = date.trim().ifBlank { todayDate() },
            location = location.trim().ifBlank { "Balcón" },
            humidity = h,
            sunPercentage = sun,
            sunHoursText = sunHoursFor(sun),
            lightLevelText = lightLevelFor(sun),
            nutrientsPercentage = n,
            npk = NPKValues(n, p, k, npkLabelFor(n, p, k)),
            status = statusFor(h, n, sun),
            iconType = iconType,
            photoUri = photoUri?.takeIf { it.isNotBlank() },
            arduinoId = arduinoDevice.id,
            sensorChannel = sensorChannel.trim().ifBlank {
                availableSensorChannels().firstOrNull() ?: SENSOR_CHANNEL_POOL.first()
            },
            isActive = true,
            remoteId = null,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(PlantActivity(title = "Planta registrada", time = activityTime()))
        )
        plants.add(plant)
        selectedPlantId = plant.id
        db { plantDao?.insertPlant(plant) }
        pushPlantCreate(plant)

        addLogEntry(
            LogType.NOTA,
            "Nueva planta registrada",
            "${plant.name} se agregó en ${plant.location} · ${plant.sensorChannel}.",
            plant.id,
            plant.name
        )
        checkThresholdAlerts(plant, h, n, sun)
    }

    fun updatePlantDetails(
        plantId: String,
        name: String,
        species: String,
        date: String,
        location: String,
        sensorChannel: String,
        iconType: PlantIconType,
        photoUri: String?
    ) {
        val old = getPlantById(plantId) ?: return
        val cleanName = name.trim().ifBlank { old.name }
        val newPhoto = photoUri?.takeIf { it.isNotBlank() }
        val activityTitle = "Datos de la planta actualizados"
        val updated = old.copy(
            name = cleanName,
            species = species.trim().ifBlank { old.species },
            plantedDate = date.trim().ifBlank { old.plantedDate },
            location = location.trim().ifBlank { old.location },
            sensorChannel = sensorChannel.trim().ifBlank { old.sensorChannel },
            iconType = iconType,
            photoUri = newPhoto,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(PlantActivity(title = activityTitle, time = activityTime())) +
                old.recentActivities
        )
        replacePlant(updated)

        // Mantiene coherente el nombre en alertas y bitácora.
        val renamed = cleanName != old.name
        val renamedAlerts = mutableListOf<HuertoAlert>()
        val renamedLogs = mutableListOf<LogEntry>()
        if (renamed) {
            for (i in alerts.indices) {
                val a = alerts[i]
                if (a.plantId == plantId) {
                    val copy = a.copy(plantName = cleanName)
                    alerts[i] = copy
                    renamedAlerts += copy
                }
            }
            for (i in logEntries.indices) {
                val e = logEntries[i]
                if (e.plantId == plantId) {
                    val copy = e.copy(plantName = cleanName)
                    logEntries[i] = copy
                    renamedLogs += copy
                }
            }
        }
        val photoToDelete = old.photoUri?.takeIf { it != newPhoto && !isPhotoReferenced(it) }

        db {
            plantDao?.updatePlant(updated)
            plantDao?.addActivity(plantId, activityTitle, activityTime())
            renamedAlerts.forEach { alertDao?.insertOrUpdateAlert(it) }
            renamedLogs.forEach { logDao?.updateEntry(it) }
            deleteManagedPhotoFile(photoToDelete)
        }
        pushPlantUpdate(updated)
    }

    fun deletePlantPermanently(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        plants.removeAll { it.id == plantId }
        alerts.removeAll { it.plantId == plantId }
        // En SQLite la FK de la bitácora es ON DELETE SET NULL.
        for (i in logEntries.indices) {
            val e = logEntries[i]
            if (e.plantId == plantId) logEntries[i] = e.copy(plantId = null)
        }
        if (selectedPlantId == plantId || getPlantById(selectedPlantId) == null) {
            selectedPlantId = activePlants().firstOrNull()?.id ?: plants.firstOrNull()?.id ?: ""
        }
        val photoToDelete = plant.photoUri?.takeIf { !isPhotoReferenced(it) }
        db {
            plantDao?.deletePlant(plantId)
            deleteManagedPhotoFile(photoToDelete)
        }

        val ctx = appContext ?: return
        CloudSyncVolleyService.syncPlantDelete(ctx, plant) { success, statusCode, message ->
            recordSync("DELETE", "/posts/${plant.remoteId ?: "1"}", "PLANT", plant.name, statusCode, success, message)
        }
    }

    fun retirePlant(plantId: String, reason: String) {
        val plant = getPlantById(plantId) ?: return
        if (!plant.isActive) return
        val cleanReason = reason.trim().ifBlank { "Sin motivo indicado" }
        val activityTitle = "Dada de baja"
        val updated = plant.copy(
            isActive = false,
            status = PlantStatus.DADA_DE_BAJA,
            retiredReason = cleanReason,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(PlantActivity(title = activityTitle, time = activityTime())) +
                plant.recentActivities
        )
        replacePlant(updated)

        val resolvedText = "Resuelta · Planta dada de baja"
        for (i in alerts.indices) {
            val a = alerts[i]
            if (a.plantId == plantId && !a.isResolved) {
                alerts[i] = a.copy(isResolved = true, resolvedTimeText = resolvedText)
            }
        }
        if (selectedPlantId == plantId) {
            activePlants().firstOrNull()?.let { selectedPlantId = it.id }
        }
        db {
            plantDao?.updatePlant(updated)
            plantDao?.addActivity(plantId, activityTitle, activityTime())
            alertDao?.resolveAlertsForPlant(plantId, null, resolvedText)
        }
        pushPlantUpdate(updated)

        addLogEntry(LogType.NOTA, "Planta dada de baja", "${plant.name}: $cleanReason", plant.id, plant.name)
    }

    fun restorePlant(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        if (plant.isActive) return
        val activityTitle = "Planta reactivada"
        val updated = plant.copy(
            isActive = true,
            retiredReason = null,
            status = statusFor(plant.humidity, plant.npk.n, plant.sunPercentage),
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(PlantActivity(title = activityTitle, time = activityTime())) +
                plant.recentActivities
        )
        replacePlant(updated)
        db {
            plantDao?.updatePlant(updated)
            plantDao?.addActivity(plantId, activityTitle, activityTime())
        }
        pushPlantUpdate(updated)

        addLogEntry(
            LogType.NOTA,
            "Planta reactivada",
            "${plant.name} vuelve a estar en seguimiento.",
            plant.id,
            plant.name
        )
    }

    // ---------------------------------------------------------------------
    // Cuidado y sensores simulados
    // ---------------------------------------------------------------------

    /** Riega las plantas activas con humedad < 85 %. Devuelve cuántas se regaron. */
    fun waterAllNow(): Int {
        val targets = activePlants().filter { it.humidity < MAX_WATERING_HUMIDITY }
        if (targets.isEmpty()) return 0
        targets.forEach { applyWatering(it.id) }
        nextWateringText = "Mañana a las 8:00"
        arduinoDevice = arduinoDevice.copy(lastSyncText = "ahora")
        addLogEntry(
            LogType.AUTOMATICO,
            "Riego general completado",
            "${targets.size} ${if (targets.size == 1) "planta regada" else "plantas regadas"} · " +
                "${targets.size * 300} ml suministrados."
        )
        return targets.size
    }

    /** Devuelve false si la planta no existe, está dada de baja o ya tiene humedad >= 85 %. */
    fun waterPlant(plantId: String): Boolean {
        val plant = getPlantById(plantId) ?: return false
        if (!plant.isActive || plant.humidity >= MAX_WATERING_HUMIDITY) return false
        applyWatering(plantId)
        arduinoDevice = arduinoDevice.copy(lastSyncText = "ahora")
        addLogEntry(
            LogType.AUTOMATICO,
            "Riego completado",
            "${plant.name} · 300 ml suministrados.",
            plant.id,
            plant.name
        )
        return true
    }

    fun fertilizePlant(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        val n = (plant.npk.n + 40).coerceIn(60, 95)
        val p = (plant.npk.p + 25).coerceIn(55, 95)
        val k = (plant.npk.k + 20).coerceIn(55, 95)
        val activityTitle = "Fertilizante aplicado"
        val updated = plant.copy(
            npk = NPKValues(n, p, k, npkLabelFor(n, p, k)),
            nutrientsPercentage = n,
            status = if (plant.isActive) statusFor(plant.humidity, n, plant.sunPercentage) else plant.status,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(PlantActivity(title = activityTitle, time = activityTime())) +
                plant.recentActivities
        )
        replacePlant(updated)
        resolveAlertsInMemory(plantId) {
            it.title.contains("Nutriente", ignoreCase = true) || it.title.contains("nitrógeno", ignoreCase = true)
        }
        db {
            plantDao?.updatePlant(updated)
            plantDao?.addActivity(plantId, activityTitle, activityTime())
            alertDao?.resolveAlertsForPlant(plantId, "nutriente")
            alertDao?.resolveAlertsForPlant(plantId, "nitrógeno")
        }
        addLogEntry(
            LogType.NOTA,
            "Fertilizante agregado",
            "Mezcla orgánica aplicada a ${plant.name.lowercase(Locale.getDefault())}.",
            plant.id,
            plant.name
        )
    }

    fun updatePlantSensorReadings(
        plantId: String,
        humidity: Int,
        sunPercentage: Int,
        n: Int,
        p: Int,
        k: Int,
        tempCelsius: Int = arduinoDevice.temperatureCelsius
    ) {
        val plant = getPlantById(plantId) ?: return
        val h = humidity.coerceIn(0, 100)
        val sun = sunPercentage.coerceIn(0, 100)
        val nn = n.coerceIn(0, 100)
        val pp = p.coerceIn(0, 100)
        val kk = k.coerceIn(0, 100)
        val newStatus = statusFor(h, nn, sun)
        val needsAttention = newStatus == PlantStatus.REQUIERE_ATENCION
        val activityTitle =
            if (needsAttention) "Lectura con valores fuera de rango" else "Lectura de sensores correcta"

        val updated = plant.copy(
            humidity = h,
            sunPercentage = sun,
            sunHoursText = sunHoursFor(sun),
            lightLevelText = lightLevelFor(sun),
            npk = NPKValues(nn, pp, kk, npkLabelFor(nn, pp, kk)),
            nutrientsPercentage = nn,
            status = if (plant.isActive) newStatus else plant.status,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(
                PlantActivity(title = activityTitle, time = activityTime(), isAlert = needsAttention)
            ) + plant.recentActivities
        )
        replacePlant(updated)
        db {
            plantDao?.updatePlant(updated)
            plantDao?.addActivity(plantId, activityTitle, activityTime(), needsAttention)
        }
        arduinoDevice = arduinoDevice.copy(
            temperatureCelsius = tempCelsius,
            lastSyncText = "ahora",
            isConnected = true
        )

        if (plant.isActive) checkThresholdAlerts(updated, h, nn, sun)

        addLogEntry(
            LogType.AUTOMATICO,
            "Lectura de sensores",
            "${plant.name} · Humedad $h% · Luz $sun% · N-P-K $nn/$pp/$kk · $tempCelsius °C",
            plant.id,
            plant.name
        )
    }

    // ---------------------------------------------------------------------
    // CRUD de alertas
    // ---------------------------------------------------------------------
    fun resolveAlert(alertId: String) {
        val index = alerts.indexOfFirst { it.id == alertId }
        if (index < 0) return
        val alert = alerts[index]
        if (alert.isResolved) return
        val resolvedText = "Resuelta · ${activityTime()}"
        alerts[index] = alert.copy(isResolved = true, resolvedTimeText = resolvedText)
        db { alertDao?.resolveAlert(alertId, resolvedText) }
        val plantStillExists = getPlantById(alert.plantId) != null
        addLogEntry(
            LogType.ALERTA,
            "Alerta resuelta",
            "${alert.title} · ${alert.plantName}",
            if (plantStillExists) alert.plantId else null,
            alert.plantName
        )
    }

    fun deleteAlert(alertId: String) {
        val removed = alerts.removeAll { it.id == alertId }
        if (removed) db { alertDao?.deleteAlert(alertId) }
    }

    // ---------------------------------------------------------------------
    // CRUD de bitácora
    // ---------------------------------------------------------------------
    fun addLogEntry(
        type: LogType,
        title: String,
        description: String,
        plantId: String? = null,
        plantName: String? = null,
        photoUri: String? = null
    ) {
        val validPlantId = plantId?.takeIf { getPlantById(it) != null }
        val entry = LogEntry(
            id = "log-${shortId()}",
            timeText = logTime(),
            type = type,
            title = title.trim().ifBlank { type.label },
            description = description.trim(),
            plantId = validPlantId,
            plantName = plantName?.trim()?.takeIf { it.isNotEmpty() }
                ?: validPlantId?.let { getPlantById(it)?.name },
            photoUri = photoUri?.takeIf { it.isNotBlank() },
            remoteId = null,
            syncStatus = SyncStatus.PENDING
        )
        logEntries.add(0, entry)
        db { logDao?.insertEntry(entry) }
        pushLogCreate(entry)
    }

    fun updateLogEntry(
        entryId: String,
        type: LogType,
        title: String,
        description: String,
        plantId: String? = null,
        plantName: String? = null,
        photoUri: String? = null
    ) {
        val index = logEntries.indexOfFirst { it.id == entryId }
        if (index < 0) return
        val old = logEntries[index]
        val validPlantId = plantId?.takeIf { getPlantById(it) != null }
        val newPhoto = photoUri?.takeIf { it.isNotBlank() }
        val updated = old.copy(
            type = type,
            title = title.trim().ifBlank { old.title },
            description = description.trim().ifBlank { old.description },
            plantId = validPlantId,
            plantName = if (validPlantId == null) {
                plantName?.trim()?.takeIf { it.isNotEmpty() }
            } else {
                plantName?.trim()?.takeIf { it.isNotEmpty() } ?: getPlantById(validPlantId)?.name
            },
            photoUri = newPhoto,
            syncStatus = SyncStatus.PENDING
        )
        logEntries[index] = updated
        val photoToDelete = old.photoUri?.takeIf { it != newPhoto && !isPhotoReferenced(it) }
        db {
            logDao?.updateEntry(updated)
            deleteManagedPhotoFile(photoToDelete)
        }
        pushLogUpdate(updated)
    }

    fun deleteLogEntry(entryId: String) {
        val entry = logEntries.firstOrNull { it.id == entryId } ?: return
        logEntries.removeAll { it.id == entryId }
        val photoToDelete = entry.photoUri?.takeIf { !isPhotoReferenced(it) }
        db {
            logDao?.deleteEntry(entryId)
            deleteManagedPhotoFile(photoToDelete)
        }
        val ctx = appContext ?: return
        CloudSyncVolleyService.syncLogDelete(ctx, entry) { success, statusCode, message ->
            recordSync("DELETE", "/posts/${entry.remoteId ?: "1"}", "LOG", entry.title, statusCode, success, message)
        }
    }

    // ---------------------------------------------------------------------
    // Clima (Open-Meteo vía Volley)
    // ---------------------------------------------------------------------
    fun refreshWeather(
        city: String = currentCity,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val ctx = appContext ?: return
        val cleanCity = city.trim().ifBlank { currentCity }
        val token = ++weatherRequestToken
        weatherState = NetworkUiState.Loading

        if (latitude != null && longitude != null) {
            fetchForecast(ctx, token, cleanCity, latitude, longitude)
            return
        }
        val known = KNOWN_CITIES[normalize(cleanCity)]
        if (known != null) {
            fetchForecast(ctx, token, cleanCity, known.first, known.second)
            return
        }
        WeatherVolleyService.searchCities(
            context = ctx,
            query = cleanCity,
            onSuccess = { options, statusCode ->
                if (token != weatherRequestToken) return@searchCities
                val first = options.firstOrNull()
                if (first == null) {
                    recordSync("GET", "/v1/search", "WEATHER", cleanCity, statusCode, false, "Ciudad no encontrada.")
                    onWeatherFailure(token, cleanCity, "No encontramos \"$cleanCity\". Prueba con otra ciudad.", false)
                } else {
                    fetchForecast(ctx, token, cleanCity, first.latitude, first.longitude)
                }
            },
            onError = { message, statusCode, isOffline ->
                if (token != weatherRequestToken) return@searchCities
                recordSync("GET", "/v1/search", "WEATHER", cleanCity, statusCode, false, message)
                onWeatherFailure(token, cleanCity, message, isOffline)
            }
        )
    }

    private fun fetchForecast(ctx: Context, token: Int, city: String, latitude: Double, longitude: Double) {
        WeatherVolleyService.fetchWeather(
            context = ctx,
            city = city,
            latitude = latitude,
            longitude = longitude,
            onSuccess = { snapshot, statusCode ->
                if (token != weatherRequestToken) return@fetchWeather
                weatherState = NetworkUiState.Success(snapshot)
                db { weatherSyncDao?.saveWeather(snapshot) }
                recordSync(
                    "GET", "/v1/forecast", "WEATHER", city, statusCode, true,
                    "Clima actualizado: ${snapshot.conditionLabel}, ${snapshot.temperatureC.toInt()} °C."
                )
            },
            onError = { message, statusCode, isOffline ->
                if (token != weatherRequestToken) return@fetchWeather
                recordSync("GET", "/v1/forecast", "WEATHER", city, statusCode, false, message)
                onWeatherFailure(token, city, message, isOffline)
            }
        )
    }

    /** Carga el último clima guardado y lo expone dentro del estado de error. */
    private fun onWeatherFailure(token: Int, city: String, message: String, isOffline: Boolean) {
        val inMemory = when (val state = weatherState) {
            is NetworkUiState.Success -> state.data
            is NetworkUiState.Error -> state.cachedData
            else -> null
        }
        mainScope.launch {
            val stored: WeatherSnapshot? = try {
                withContext(dbDispatcher) {
                    weatherSyncDao?.let { it.getLatestWeatherForCity(city) ?: it.getLatestAnyWeather() }
                }
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo leer el clima guardado", e)
                null
            }
            val cached = stored ?: inMemory
            if (token != weatherRequestToken) return@launch
            weatherState = NetworkUiState.Error(
                message = message,
                cachedData = cached?.copy(isFromCache = true),
                isOffline = isOffline
            )
        }
    }

    fun searchCities(query: String) {
        val check = Validators.validateCityName(query)
        if (!check.isValid) {
            citySearchState = NetworkUiState.Error(check.errorMessage ?: "Ingresa una ciudad válida.")
            return
        }
        val ctx = appContext ?: return
        citySearchState = NetworkUiState.Loading
        val clean = query.trim()
        WeatherVolleyService.searchCities(
            context = ctx,
            query = clean,
            onSuccess = { options, _ ->
                citySearchState = if (options.isEmpty()) {
                    NetworkUiState.Error("No encontramos ciudades con \"$clean\".")
                } else {
                    NetworkUiState.Success(options)
                }
            },
            onError = { message, _, isOffline ->
                citySearchState = NetworkUiState.Error(message, null, isOffline)
            }
        )
    }

    fun selectCityAndFetchWeather(option: CityOption) {
        val cityName = option.name.trim().ifBlank { return }
        currentCity = cityName
        val user = currentUser
        if (user != null) {
            currentUser = user.copy(city = cityName)
            db { userDao?.updateUserCity(user.id, cityName) }
        }
        citySearchState = NetworkUiState.Idle
        refreshWeather(cityName, option.latitude, option.longitude)
    }

    // ---------------------------------------------------------------------
    // Sincronización con la nube
    // ---------------------------------------------------------------------
    fun syncPendingItems() {
        if (isSyncingCloud) return
        val ctx = appContext ?: return
        val pendingPlants = plants.filter { it.syncStatus != SyncStatus.SYNCED }
        val pendingLogs = logEntries.filter { it.syncStatus != SyncStatus.SYNCED }
        val total = pendingPlants.size + pendingLogs.size
        if (total == 0) {
            lastSyncMessage = "Todo está respaldado en la nube."
            return
        }
        if (!VolleySingleton.isNetworkAvailable(ctx)) {
            lastSyncMessage = "Sin conexión. Hay $total ${if (total == 1) "cambio pendiente" else "cambios pendientes"}."
            return
        }
        isSyncingCloud = true
        lastSyncMessage = "Sincronizando $total ${if (total == 1) "elemento" else "elementos"}..."
        var remaining = total
        var ok = 0
        val onItemDone: (Boolean) -> Unit = { success ->
            if (success) ok++
            remaining--
            if (remaining <= 0) {
                isSyncingCloud = false
                lastSyncMessage = if (ok == total) {
                    "Listo: $ok de $total elementos respaldados en la nube."
                } else {
                    "Se respaldaron $ok de $total elementos. Los demás quedan pendientes."
                }
            }
        }
        pendingPlants.forEach { plant ->
            if (plant.remoteId == null) pushPlantCreate(plant, onItemDone) else pushPlantUpdate(plant, onItemDone)
        }
        pendingLogs.forEach { entry ->
            if (entry.remoteId == null) pushLogCreate(entry, onItemDone) else pushLogUpdate(entry, onItemDone)
        }
    }

    fun pendingSyncCount(): Int =
        plants.count { it.syncStatus != SyncStatus.SYNCED } +
            logEntries.count { it.syncStatus != SyncStatus.SYNCED }

    fun syncedPercentage(): Int {
        val total = plants.size + logEntries.size
        if (total == 0) return 100
        val synced = plants.count { it.syncStatus == SyncStatus.SYNCED } +
            logEntries.count { it.syncStatus == SyncStatus.SYNCED }
        return (synced * 100) / total
    }

    private fun statusAfter(success: Boolean, statusCode: Int): SyncStatus = when {
        success -> SyncStatus.SYNCED
        statusCode == ApiConfig.NO_HTTP_STATUS -> SyncStatus.PENDING
        else -> SyncStatus.ERROR
    }

    private fun pushPlantCreate(plant: Plant, onDone: ((Boolean) -> Unit)? = null) {
        val ctx = appContext ?: run { onDone?.invoke(false); return }
        CloudSyncVolleyService.syncPlantCreate(ctx, plant) { success, statusCode, remoteId, message ->
            applyPlantSyncResult(plant.id, success, statusCode, remoteId)
            recordSync("POST", "/posts", "PLANT", plant.name, statusCode, success, message)
            onDone?.invoke(success)
        }
    }

    /** PUT si la planta ya tiene id remoto; si nunca se respaldó, la crea con POST. */
    private fun pushPlantUpdate(plant: Plant, onDone: ((Boolean) -> Unit)? = null) {
        if (plant.remoteId == null) {
            pushPlantCreate(plant, onDone)
            return
        }
        val ctx = appContext ?: run { onDone?.invoke(false); return }
        CloudSyncVolleyService.syncPlantUpdate(ctx, plant) { success, statusCode, message ->
            applyPlantSyncResult(plant.id, success, statusCode, null)
            recordSync("PUT", "/posts/${plant.remoteId}", "PLANT", plant.name, statusCode, success, message)
            onDone?.invoke(success)
        }
    }

    private fun applyPlantSyncResult(plantId: String, success: Boolean, statusCode: Int, remoteId: String?) {
        val status = statusAfter(success, statusCode)
        val index = plants.indexOfFirst { it.id == plantId }
        if (index < 0) return // eliminada mientras se sincronizaba
        val current = plants[index]
        plants[index] = current.copy(remoteId = remoteId ?: current.remoteId, syncStatus = status)
        db { plantDao?.updateSyncStatus(plantId, remoteId, status) }
    }

    private fun pushLogCreate(entry: LogEntry, onDone: ((Boolean) -> Unit)? = null) {
        val ctx = appContext ?: run { onDone?.invoke(false); return }
        CloudSyncVolleyService.syncLogCreate(ctx, entry) { success, statusCode, remoteId, message ->
            applyLogSyncResult(entry.id, success, statusCode, remoteId)
            recordSync("POST", "/posts", "LOG", entry.title, statusCode, success, message)
            onDone?.invoke(success)
        }
    }

    private fun pushLogUpdate(entry: LogEntry, onDone: ((Boolean) -> Unit)? = null) {
        if (entry.remoteId == null) {
            pushLogCreate(entry, onDone)
            return
        }
        val ctx = appContext ?: run { onDone?.invoke(false); return }
        CloudSyncVolleyService.syncLogUpdate(ctx, entry) { success, statusCode, message ->
            applyLogSyncResult(entry.id, success, statusCode, null)
            recordSync("PUT", "/posts/${entry.remoteId}", "LOG", entry.title, statusCode, success, message)
            onDone?.invoke(success)
        }
    }

    private fun applyLogSyncResult(entryId: String, success: Boolean, statusCode: Int, remoteId: String?) {
        val status = statusAfter(success, statusCode)
        val index = logEntries.indexOfFirst { it.id == entryId }
        if (index < 0) return
        val current = logEntries[index]
        logEntries[index] = current.copy(remoteId = remoteId ?: current.remoteId, syncStatus = status)
        db { logDao?.updateSyncStatus(entryId, remoteId, status) }
    }

    private fun recordSync(
        method: String,
        endpoint: String,
        entityType: String,
        entityLabel: String,
        statusCode: Int,
        success: Boolean,
        message: String
    ) {
        val record = SyncRecord(
            httpMethod = method,
            endpoint = endpoint,
            entityType = entityType,
            entityLabel = entityLabel,
            statusCode = statusCode,
            success = success,
            message = message,
            createdAtText = logTime()
        )
        syncHistory.add(0, record)
        while (syncHistory.size > SYNC_HISTORY_LIMIT) syncHistory.removeAt(syncHistory.lastIndex)
        db { weatherSyncDao?.insertSyncRecord(record) }
    }

    // ---------------------------------------------------------------------
    // Interpretaciones en lenguaje sencillo
    // ---------------------------------------------------------------------
    fun interpretHumidity(humidity: Int): String = when {
        humidity < 30 -> "Crítico: suelo muy seco. Riega ahora (300 ml)."
        humidity <= 45 -> "El suelo está seco. Conviene un riego leve hoy."
        humidity <= 80 -> "Humedad óptima. Las raíces absorben bien el agua."
        else -> "Exceso de humedad. Suspende el riego por 48 h."
    }

    fun interpretLight(sunPercentage: Int, hoursText: String): String =
        "Ha recibido $hoursText. " +
            if (sunPercentage < 45) "Nivel bajo: acerca la maceta a la ventana o al balcón."
            else "Nivel adecuado."

    fun interpretNPK(npk: NPKValues): String {
        val parts = mutableListOf<String>()
        parts += when {
            npk.n < 30 -> "Nitrógeno bajo (${npk.n}%): las hojas pueden ponerse amarillas, conviene fertilizar."
            npk.n < 60 -> "Nitrógeno moderado (${npk.n}%): el follaje crece, pero puede mejorar."
            else -> "Nitrógeno bien (${npk.n}%): hojas verdes y crecimiento activo."
        }
        parts += when {
            npk.p < 30 -> "Fósforo bajo (${npk.p}%): raíces y flores se desarrollan más lento."
            npk.p < 60 -> "Fósforo moderado (${npk.p}%): suficiente para raíces, justo para florecer."
            else -> "Fósforo bien (${npk.p}%): raíces fuertes y buena floración."
        }
        parts += when {
            npk.k < 30 -> "Potasio bajo (${npk.k}%): la planta resiste peor el calor y las plagas."
            npk.k < 60 -> "Potasio moderado (${npk.k}%): defensas aceptables."
            else -> "Potasio bien (${npk.k}%): buenas defensas y frutos de calidad."
        }
        val summary = if (minOf(npk.n, npk.p, npk.k) < 30) {
            "Recomendación: agrega fertilizante orgánico esta semana."
        } else {
            "No hace falta fertilizar por ahora."
        }
        return parts.joinToString(" ") + " " + summary
    }

    fun statusFor(humidity: Int, n: Int, sun: Int): PlantStatus =
        if (humidity < HUMIDITY_MIN || n < NITROGEN_MIN || sun < SUN_MIN) PlantStatus.REQUIERE_ATENCION
        else PlantStatus.SALUDABLE

    fun npkLabelFor(n: Int, p: Int, k: Int): String = when {
        minOf(n, p, k) < 30 -> "Bajos"
        (n + p + k) / 3 >= 65 -> "Óptimos"
        else -> "Medios"
    }

    fun lightLevelFor(sun: Int): String = when {
        sun < 45 -> "Baja"
        sun < 70 -> "Media"
        sun < 80 -> "Alta"
        else -> "Óptima"
    }

    /** Aproxima horas de sol a partir del porcentaje (100 % ≈ 7 h 30 min). */
    fun sunHoursFor(sun: Int): String {
        val totalMinutes = (sun.coerceIn(0, 100) * 450) / 100
        val h = totalMinutes / 60
        val m = (totalMinutes % 60) / 5 * 5
        return if (m == 0) "$h h de sol" else "$h h $m min de sol"
    }

    // ---------------------------------------------------------------------
    // Helpers internos
    // ---------------------------------------------------------------------

    /** Ejecuta trabajo de SQLite en IO (serializado) sin bloquear ni romper la UI. */
    private fun db(block: () -> Unit) {
        mainScope.launch(dbDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                Log.e(TAG, "Error de SQLite", e)
            }
        }
    }

    private fun replacePlant(updated: Plant) {
        val index = plants.indexOfFirst { it.id == updated.id }
        if (index >= 0) plants[index] = updated
    }

    private fun applyWatering(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        if (plant.humidity >= MAX_WATERING_HUMIDITY) return
        val newHumidity = (plant.humidity + 35).coerceIn(65, MAX_WATERING_HUMIDITY)
        val activityTitle = "Riego manual"
        val updated = plant.copy(
            humidity = newHumidity,
            status = if (plant.isActive) statusFor(newHumidity, plant.npk.n, plant.sunPercentage) else plant.status,
            updatedAt = System.currentTimeMillis(),
            recentActivities = listOf(PlantActivity(title = activityTitle, time = activityTime())) +
                plant.recentActivities
        )
        replacePlant(updated)
        resolveAlertsInMemory(plantId) { it.title.contains("Humedad", ignoreCase = true) }
        db {
            plantDao?.updatePlant(updated)
            plantDao?.addActivity(plantId, activityTitle, activityTime())
            alertDao?.resolveAlertsForPlant(plantId, "humedad")
        }
    }

    private inline fun resolveAlertsInMemory(plantId: String, predicate: (HuertoAlert) -> Boolean) {
        for (i in alerts.indices) {
            val a = alerts[i]
            if (a.plantId == plantId && !a.isResolved && predicate(a)) {
                alerts[i] = a.copy(isResolved = true, resolvedTimeText = "Resuelta · Ahora")
            }
        }
    }

    private fun checkThresholdAlerts(plant: Plant, humidity: Int, n: Int, sun: Int) {
        if (!plant.isActive) return
        if (humidity < HUMIDITY_MIN) {
            pushAlert(
                severity = if (humidity < 25) AlertSeverity.CRITICA else AlertSeverity.AVISO,
                title = "Humedad muy baja",
                plant = plant,
                description = "El sensor registra $humidity%. Recomendamos regar ahora."
            )
        }
        if (n < NITROGEN_MIN) {
            pushAlert(
                severity = AlertSeverity.AVISO,
                title = "Nutrientes bajos",
                plant = plant,
                description = "Nitrógeno en $n%. Agrega fertilizante en los próximos 2 días."
            )
        }
        if (sun < SUN_MIN) {
            pushAlert(
                severity = AlertSeverity.CONSEJO,
                title = "Iluminación mejorable",
                plant = plant,
                description = "Solo $sun% de luz. Mueve la planta a una zona con más sol directo."
            )
        }
    }

    private fun pushAlert(severity: AlertSeverity, title: String, plant: Plant, description: String) {
        val existing = alerts.indexOfFirst { !it.isResolved && it.plantId == plant.id && it.title == title }
        val alert = if (existing >= 0) {
            alerts[existing].copy(severity = severity, description = description, timeText = "Ahora")
                .also { alerts[existing] = it }
        } else {
            HuertoAlert(
                id = "al-${shortId()}",
                severity = severity,
                title = title,
                plantId = plant.id,
                plantName = plant.name,
                description = description,
                timeText = "Ahora"
            ).also { alerts.add(0, it) }
        }
        db { alertDao?.insertOrUpdateAlert(alert) }
    }

    private fun isPhotoReferenced(uri: String): Boolean =
        plants.any { it.photoUri == uri } || logEntries.any { it.photoUri == uri }

    /** Borra solo fotos creadas por la app (filesDir/photos). Debe llamarse en IO. */
    private fun deleteManagedPhotoFile(uri: String?) {
        if (uri.isNullOrBlank()) return
        val ctx = appContext ?: return
        try {
            val photosDir = File(ctx.filesDir, "photos")
            val parsed = Uri.parse(uri)
            val file: File = when {
                uri.startsWith("content://${ctx.packageName}.fileprovider/natura_photos/") ->
                    File(photosDir, parsed.lastPathSegment ?: return)
                uri.startsWith("file://") -> File(parsed.path ?: return)
                uri.startsWith("/") -> File(uri)
                else -> return
            }
            if (file.canonicalPath.startsWith(photosDir.canonicalPath + File.separator) && file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo borrar la foto $uri", e)
        }
    }

    private fun failure(message: String): Result<UserAccount> =
        Result.failure(IllegalArgumentException(message))

    private fun shortId(): String = UUID.randomUUID().toString().replace("-", "").take(8)

    private fun normalize(text: String): String =
        Normalizer.normalize(text.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

    private fun slugify(text: String): String =
        normalize(text)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(24)

    private fun nowTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun logTime(): String = "Hoy · ${nowTime()}"

    private fun activityTime(): String = "Hoy, ${nowTime()}"

    private fun todayDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}
