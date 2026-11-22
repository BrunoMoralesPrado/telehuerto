package com.example.telehuerto.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fuente única de datos de la app. Usa estado de Compose para que la UI
 * se recomponga sola cuando algo cambia.
 */
object HuertoRepository {

    // ---------------------------------------------------------------------
    // Estado de sesión y dispositivo
    // ---------------------------------------------------------------------
    var currentUserName by mutableStateOf("Andrea")
    var currentUserEmail by mutableStateOf("andrea@huerto.com")
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
            assignedUserEmail = "andrea@huerto.com"
        )
    )

    // ---------------------------------------------------------------------
    // Plantas
    // ---------------------------------------------------------------------
    val plants = mutableStateListOf(
        Plant(
            id = "tomate",
            name = "Tomate cherry",
            species = "Solanum lycopersicum",
            plantedDate = "15/09/2026",
            location = "Balcón",
            humidity = 78,
            sunPercentage = 82,
            sunHoursText = "6 h 15 min de sol",
            lightLevelText = "Óptima",
            nutrientsPercentage = 74,
            npk = NPKValues(74, 70, 78, "Óptimos"),
            status = PlantStatus.SALUDABLE,
            iconType = PlantIconType.TOMATE,
            arduinoId = "ARD-01",
            sensorChannel = "Canal A1 (Sonda Suelo + Luz)",
            recentActivities = listOf(
                PlantActivity("Riego automático", "Hoy, 08:00", false),
                PlantActivity("Humedad óptima verificada", "Ayer, 18:30", false)
            )
        ),
        Plant(
            id = "albahaca",
            name = "Albahaca genovesa",
            species = "Ocimum basilicum",
            plantedDate = "02/10/2026",
            location = "Balcón",
            humidity = 40,
            sunPercentage = 66,
            sunHoursText = "4 h 30 min de sol",
            lightLevelText = "Media",
            nutrientsPercentage = 24,
            npk = NPKValues(24, 48, 61, "Bajos"),
            status = PlantStatus.REQUIERE_ATENCION,
            iconType = PlantIconType.ALBAHACA,
            arduinoId = "ARD-01",
            sensorChannel = "Canal A2 (Sonda NPK + Humedad)",
            recentActivities = listOf(
                PlantActivity("Nutrientes bajos detectados", "Hoy, 09:42", true),
                PlantActivity("Riego automático", "Ayer, 08:00", false)
            )
        ),
        Plant(
            id = "pimiento",
            name = "Pimiento rojo",
            species = "Capsicum annuum",
            plantedDate = "20/09/2026",
            location = "Jardín",
            humidity = 72,
            sunPercentage = 76,
            sunHoursText = "5 h 40 min de sol",
            lightLevelText = "Alta",
            nutrientsPercentage = 81,
            npk = NPKValues(81, 75, 84, "Óptimos"),
            status = PlantStatus.SALUDABLE,
            iconType = PlantIconType.PIMIENTO,
            arduinoId = "ARD-01",
            sensorChannel = "Canal A3 (Sonda Multiparámetro)",
            recentActivities = listOf(
                PlantActivity("Humedad recuperada", "Ayer, 10:32", false),
                PlantActivity("Riego automático", "Ayer, 08:00", false)
            )
        )
    )

    // ---------------------------------------------------------------------
    // Alertas
    // ---------------------------------------------------------------------
    val alerts = mutableStateListOf(
        HuertoAlert(
            id = "al-1",
            severity = AlertSeverity.CRITICA,
            title = "Humedad muy baja",
            plantId = "tomate",
            plantName = "Tomate cherry",
            description = "El sensor registra 18%. Recomendamos regar ahora.",
            timeText = "Hace 2 h"
        ),
        HuertoAlert(
            id = "al-2",
            severity = AlertSeverity.AVISO,
            title = "Nutrientes bajos",
            plantId = "albahaca",
            plantName = "Albahaca genovesa",
            description = "Nivel de nitrógeno bajo. Agrega fertilizante en 2 días.",
            timeText = "Hace 6 h"
        ),
        HuertoAlert(
            id = "al-3",
            severity = AlertSeverity.CONSEJO,
            title = "Iluminación mejorable",
            plantId = "pimiento",
            plantName = "Pimiento rojo",
            description = "Mueve la planta a una zona con más sol directo.",
            timeText = "Ayer"
        ),
        HuertoAlert(
            id = "al-4",
            severity = AlertSeverity.AVISO,
            title = "Humedad recuperada",
            plantId = "pimiento",
            plantName = "Pimiento rojo",
            description = "El nivel volvió a 72% después del último riego.",
            timeText = "Ayer, 10:32",
            isResolved = true,
            resolvedTimeText = "Resuelta · Ayer, 10:32"
        ),
        HuertoAlert(
            id = "al-5",
            severity = AlertSeverity.CONSEJO,
            title = "Temperatura estabilizada",
            plantId = "tomate",
            plantName = "Tomate cherry",
            description = "La temperatura se mantiene dentro del rango recomendado.",
            timeText = "10 oct, 18:15",
            isResolved = true,
            resolvedTimeText = "Resuelta · 10 oct, 18:15"
        )
    )

    // ---------------------------------------------------------------------
    // Bitácora
    // ---------------------------------------------------------------------
    val logEntries = mutableStateListOf(
        LogEntry(
            "log-1", "Hoy · 10:24", LogType.FOTO, "Crecimiento de albahaca",
            "Nuevas hojas visibles. La planta responde bien.", "albahaca", "Albahaca genovesa"
        ),
        LogEntry(
            "log-2", "Hoy · 08:00", LogType.AUTOMATICO, "Riego completado",
            "Tomate cherry · 350 ml suministrados.", "tomate", "Tomate cherry"
        ),
        LogEntry(
            "log-3", "Ayer · 17:36", LogType.NOTA, "Fertilizante agregado",
            "Mezcla orgánica aplicada a la albahaca.", "albahaca", "Albahaca genovesa"
        ),
        LogEntry(
            "log-4", "12 oct · 09:18", LogType.ALERTA, "Humedad recuperada",
            "Pimiento rojo volvió a niveles saludables.", "pimiento", "Pimiento rojo"
        )
    )

    // Contadores para generar ids únicos
    private var alertCounter = 5
    private var logCounter = 4
    private var plantCounter = 0

    // Umbrales de salud
    private const val HUMIDITY_MIN = 35
    private const val NITROGEN_MIN = 30
    private const val SUN_MIN = 40

    // ---------------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------------
    fun activePlants(): List<Plant> = plants.filter { it.isActive }

    fun retiredPlants(): List<Plant> = plants.filter { !it.isActive }

    fun activeAlerts(): List<HuertoAlert> = alerts.filter { !it.isResolved }

    fun resolvedAlerts(): List<HuertoAlert> = alerts.filter { it.isResolved }

    fun getPlantById(id: String): Plant? = plants.firstOrNull { it.id == id }

    fun getSelectedPlant(): Plant =
        getPlantById(selectedPlantId)
            ?: activePlants().firstOrNull()
            ?: plants.first()

    fun selectPlant(id: String) {
        if (getPlantById(id) != null) selectedPlantId = id
    }

    // ---------------------------------------------------------------------
    // Sesión
    // ---------------------------------------------------------------------
    fun loginUser(email: String, name: String = "Andrea") {
        val cleanEmail = email.trim().ifBlank { "andrea@huerto.com" }
        currentUserEmail = cleanEmail
        currentUserName = name.trim().ifBlank {
            cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        arduinoDevice = arduinoDevice.copy(assignedUserEmail = cleanEmail)
    }

    // ---------------------------------------------------------------------
    // Gestión de plantas
    // ---------------------------------------------------------------------
    fun addPlant(
        name: String,
        species: String,
        date: String,
        location: String,
        sensorChannel: String,
        iconType: PlantIconType = PlantIconType.GENERIC
    ) {
        val cleanName = name.trim().ifBlank { "Nueva planta" }
        plantCounter++
        val baseId = slugify(cleanName).ifBlank { "planta" }
        var newId = baseId
        if (getPlantById(newId) != null) newId = "$baseId-$plantCounter"
        while (getPlantById(newId) != null) {
            plantCounter++
            newId = "$baseId-$plantCounter"
        }

        val humidity = 65
        val sun = 70
        val npk = NPKValues(70, 65, 70, "Óptimos")
        val plant = Plant(
            id = newId,
            name = cleanName,
            species = species.trim().ifBlank { "Especie sin definir" },
            plantedDate = date.trim().ifBlank { todayDate() },
            location = location.trim().ifBlank { "Balcón" },
            humidity = humidity,
            sunPercentage = sun,
            sunHoursText = sunHoursFor(sun),
            lightLevelText = lightLevelFor(sun),
            nutrientsPercentage = npk.n,
            npk = npk,
            status = PlantStatus.SALUDABLE,
            iconType = iconType,
            arduinoId = arduinoDevice.id,
            sensorChannel = sensorChannel.trim().ifBlank { "Canal A${plants.size + 1}" },
            recentActivities = listOf(PlantActivity("Planta registrada", activityTime()))
        )
        plants.add(plant)
        selectedPlantId = plant.id
        addLogEntry(
            LogType.NOTA,
            "Nueva planta registrada",
            "${plant.name} se agregó en ${plant.location} · ${plant.sensorChannel}.",
            plant.id,
            plant.name
        )
    }

    fun retirePlant(plantId: String, reason: String) {
        val plant = getPlantById(plantId) ?: return
        val cleanReason = reason.trim().ifBlank { "Sin motivo indicado" }
        updatePlant(plantId) {
            it.copy(
                isActive = false,
                status = PlantStatus.DADA_DE_BAJA,
                retiredReason = cleanReason,
                recentActivities = listOf(PlantActivity("Dada de baja", activityTime())) + it.recentActivities
            )
        }
        // Las alertas abiertas de una planta retirada ya no aplican.
        alerts.indices.forEach { i ->
            val a = alerts[i]
            if (a.plantId == plantId && !a.isResolved) {
                alerts[i] = a.copy(isResolved = true, resolvedTimeText = "Resuelta · Planta dada de baja")
            }
        }
        if (selectedPlantId == plantId) {
            activePlants().firstOrNull()?.let { selectedPlantId = it.id }
        }
        addLogEntry(
            LogType.NOTA,
            "Planta dada de baja",
            "${plant.name}: $cleanReason",
            plant.id,
            plant.name
        )
    }

    fun restorePlant(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        updatePlant(plantId) {
            it.copy(
                isActive = true,
                retiredReason = null,
                status = statusFor(it.humidity, it.npk.n, it.sunPercentage),
                recentActivities = listOf(PlantActivity("Planta reactivada", activityTime())) + it.recentActivities
            )
        }
        addLogEntry(
            LogType.NOTA,
            "Planta reactivada",
            "${plant.name} vuelve a estar en seguimiento.",
            plant.id,
            plant.name
        )
    }

    // ---------------------------------------------------------------------
    // Acciones de cuidado
    // ---------------------------------------------------------------------
    fun waterAllNow() {
        val targets = activePlants()
        if (targets.isEmpty()) return
        targets.forEach { applyWatering(it.id) }
        nextWateringText = "Mañana a las 8:00"
        arduinoDevice = arduinoDevice.copy(lastSyncText = "ahora")
        addLogEntry(
            LogType.AUTOMATICO,
            "Riego general completado",
            "${targets.size} plantas regadas · ${targets.size * 300} ml suministrados."
        )
    }

    fun waterPlant(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        applyWatering(plantId)
        arduinoDevice = arduinoDevice.copy(lastSyncText = "ahora")
        addLogEntry(
            LogType.AUTOMATICO,
            "Riego completado",
            "${plant.name} · 300 ml suministrados.",
            plant.id,
            plant.name
        )
    }

    fun fertilizePlant(plantId: String) {
        val plant = getPlantById(plantId) ?: return
        updatePlant(plantId) {
            val n = (it.npk.n + 40).coerceIn(60, 95)
            val p = (it.npk.p + 25).coerceIn(55, 95)
            val k = (it.npk.k + 20).coerceIn(55, 95)
            val npk = NPKValues(n, p, k, npkLabelFor(n, p, k))
            it.copy(
                npk = npk,
                nutrientsPercentage = n,
                status = if (it.isActive) statusFor(it.humidity, n, it.sunPercentage) else it.status,
                recentActivities = listOf(PlantActivity("Fertilizante aplicado", activityTime())) + it.recentActivities
            )
        }
        resolveAlertsFor(plantId) { it.title.contains("Nutriente", ignoreCase = true) || it.title.contains("nitrógeno", ignoreCase = true) }
        addLogEntry(
            LogType.NOTA,
            "Fertilizante agregado",
            "Mezcla orgánica aplicada a ${plant.name.lowercase()}.",
            plant.id,
            plant.name
        )
    }

    fun resolveAlert(alertId: String) {
        val index = alerts.indexOfFirst { it.id == alertId }
        if (index < 0) return
        val alert = alerts[index]
        if (alert.isResolved) return
        alerts[index] = alert.copy(isResolved = true, resolvedTimeText = "Resuelta · Ahora")
        addLogEntry(
            LogType.ALERTA,
            "Alerta resuelta",
            "${alert.title} · ${alert.plantName}",
            alert.plantId,
            alert.plantName
        )
    }

    fun addLogEntry(
        type: LogType,
        title: String,
        description: String,
        plantId: String? = null,
        plantName: String? = null
    ) {
        logCounter++
        logEntries.add(
            0,
            LogEntry(
                id = "log-$logCounter",
                timeText = logTime(),
                type = type,
                title = title,
                description = description,
                plantId = plantId,
                plantName = plantName
            )
        )
    }

    // ---------------------------------------------------------------------
    // Lecturas del Arduino
    // ---------------------------------------------------------------------
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

        updatePlant(plantId) {
            it.copy(
                humidity = h,
                sunPercentage = sun,
                sunHoursText = sunHoursFor(sun),
                lightLevelText = lightLevelFor(sun),
                npk = NPKValues(nn, pp, kk, npkLabelFor(nn, pp, kk)),
                nutrientsPercentage = nn,
                status = if (it.isActive) newStatus else it.status,
                recentActivities = listOf(
                    PlantActivity(
                        if (needsAttention) "Lectura con valores fuera de rango" else "Lectura de sensores correcta",
                        activityTime(),
                        needsAttention
                    )
                ) + it.recentActivities
            )
        }
        arduinoDevice = arduinoDevice.copy(
            temperatureCelsius = tempCelsius,
            lastSyncText = "ahora",
            isConnected = true
        )

        // Alertas por cada umbral superado (solo si no hay una igual abierta)
        if (plant.isActive) {
            if (h < HUMIDITY_MIN) {
                pushAlert(
                    severity = if (h < 25) AlertSeverity.CRITICA else AlertSeverity.AVISO,
                    title = "Humedad muy baja",
                    plant = plant,
                    description = "El sensor registra $h%. Recomendamos regar ahora."
                )
            }
            if (nn < NITROGEN_MIN) {
                pushAlert(
                    severity = AlertSeverity.AVISO,
                    title = "Nutrientes bajos",
                    plant = plant,
                    description = "Nitrógeno en $nn%. Agrega fertilizante en los próximos 2 días."
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

        addLogEntry(
            LogType.AUTOMATICO,
            "Lectura de sensores",
            "${plant.name} · Humedad $h% · Luz $sun% · N-P-K $nn/$pp/$kk · $tempCelsius °C",
            plant.id,
            plant.name
        )
    }

    // ---------------------------------------------------------------------
    // Interpretaciones en lenguaje sencillo
    // ---------------------------------------------------------------------
    fun interpretHumidity(humidity: Int): String = when {
        humidity < 30 -> "Crítico: Suelo muy seco. Regar inmediatamente (300 ml)."
        humidity <= 45 -> "El suelo está seco. Recomendamos un riego leve hoy."
        humidity <= 80 -> "Humedad óptima. Las raíces absorben agua adecuadamente."
        else -> "Exceso de humedad. Suspender riego por 48 h."
    }

    fun interpretLight(sunPercentage: Int, hoursText: String): String =
        "Ha recibido $hoursText de luz. " +
            if (sunPercentage < 45) "Nivel bajo: acerca la maceta a la ventana o balcón."
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

    // ---------------------------------------------------------------------
    // Helpers internos
    // ---------------------------------------------------------------------
    private inline fun updatePlant(plantId: String, transform: (Plant) -> Plant) {
        val index = plants.indexOfFirst { it.id == plantId }
        if (index >= 0) plants[index] = transform(plants[index])
    }

    private fun applyWatering(plantId: String) {
        updatePlant(plantId) {
            val newHumidity = if (it.humidity >= 85) it.humidity else (it.humidity + 35).coerceIn(65, 85)
            it.copy(
                humidity = newHumidity,
                status = if (it.isActive) statusFor(newHumidity, it.npk.n, it.sunPercentage) else it.status,
                recentActivities = listOf(PlantActivity("Riego manual", activityTime())) + it.recentActivities
            )
        }
        resolveAlertsFor(plantId) { it.title.contains("Humedad", ignoreCase = true) }
    }

    private inline fun resolveAlertsFor(plantId: String, predicate: (HuertoAlert) -> Boolean) {
        for (i in alerts.indices) {
            val a = alerts[i]
            if (a.plantId == plantId && !a.isResolved && predicate(a)) {
                alerts[i] = a.copy(isResolved = true, resolvedTimeText = "Resuelta · Ahora")
            }
        }
    }

    private fun pushAlert(severity: AlertSeverity, title: String, plant: Plant, description: String) {
        val existing = alerts.indexOfFirst { !it.isResolved && it.plantId == plant.id && it.title == title }
        if (existing >= 0) {
            // Actualiza la alerta abierta con el dato más reciente
            alerts[existing] = alerts[existing].copy(
                severity = severity,
                description = description,
                timeText = "Ahora"
            )
            return
        }
        alertCounter++
        alerts.add(
            0,
            HuertoAlert(
                id = "al-$alertCounter",
                severity = severity,
                title = title,
                plantId = plant.id,
                plantName = plant.name,
                description = description,
                timeText = "Ahora"
            )
        )
    }

    private fun statusFor(humidity: Int, n: Int, sun: Int): PlantStatus =
        if (humidity < HUMIDITY_MIN || n < NITROGEN_MIN || sun < SUN_MIN) PlantStatus.REQUIERE_ATENCION
        else PlantStatus.SALUDABLE

    private fun npkLabelFor(n: Int, p: Int, k: Int): String = when {
        minOf(n, p, k) < 30 -> "Bajos"
        (n + p + k) / 3 >= 65 -> "Óptimos"
        else -> "Medios"
    }

    private fun lightLevelFor(sun: Int): String = when {
        sun < 45 -> "Baja"
        sun < 70 -> "Media"
        sun < 80 -> "Alta"
        else -> "Óptima"
    }

    /** Aproxima horas de sol a partir del porcentaje (100% ≈ 7 h 30 min). */
    private fun sunHoursFor(sun: Int): String {
        val totalMinutes = (sun.coerceIn(0, 100) * 450) / 100
        val h = totalMinutes / 60
        val m = (totalMinutes % 60) / 5 * 5
        return if (m == 0) "$h h de sol" else "$h h $m min de sol"
    }

    private fun slugify(text: String): String {
        val map = mapOf('á' to 'a', 'é' to 'e', 'í' to 'i', 'ó' to 'o', 'ú' to 'u', 'ü' to 'u', 'ñ' to 'n')
        return text.lowercase()
            .map { map[it] ?: it }
            .joinToString("")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private fun nowTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun logTime(): String = "Hoy · ${nowTime()}"

    private fun activityTime(): String = "Hoy, ${nowTime()}"

    private fun todayDate(): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
}
