package com.example.telehuerto.data

enum class PlantStatus(val label: String) {
    SALUDABLE("Saludable"),
    REQUIERE_ATENCION("Requiere atención"),
    DADA_DE_BAJA("Dada de baja")
}

enum class AlertSeverity(val label: String) {
    CRITICA("CRÍTICA"),
    AVISO("AVISO"),
    CONSEJO("CONSEJO")
}

enum class LogType(val label: String) {
    FOTO("FOTO"),
    AUTOMATICO("AUTOMÁTICO"),
    NOTA("NOTA"),
    ALERTA("ALERTA")
}

enum class SyncStatus(val label: String) {
    SYNCED("Sincronizada"),
    PENDING("Pendiente"),
    ERROR("Error de red")
}

enum class PlantIconType(val displayName: String, val defaultSpecies: String) {
    TOMATE("Tomate cherry", "Solanum lycopersicum"),
    ALBAHACA("Albahaca", "Ocimum basilicum"),
    PIMIENTO("Pimiento rojo", "Capsicum annuum"),
    CEBOLLA("Cebolla", "Allium cepa"),
    PAPA("Papa", "Solanum tuberosum"),
    MANZANA("Manzano", "Malus domestica"),
    ZANAHORIA("Zanahoria", "Daucus carota"),
    FRESA("Fresa", "Fragaria × ananassa"),
    LECHUGA("Lechuga", "Lactuca sativa"),
    MENTA("Menta", "Mentha spicata"),
    CHILE("Chile jalapeño", "Capsicum annuum"),
    LIMON("Limonero", "Citrus × limon"),
    AJO("Ajo", "Allium sativum"),
    GENERIC("Brote general", "Planta de huerto")
}

data class UserAccount(
    val id: Long = 0L,
    val name: String,
    val email: String,
    val passwordHash: String = "",
    val arduinoSerial: String = "ARD-01",
    val city: String = "Ciudad de México",
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class NPKValues(
    val n: Int,
    val p: Int,
    val k: Int,
    val statusLabel: String = "Bajos"
)

data class PlantActivity(
    val id: Long = 0L,
    val title: String,
    val time: String,
    val isAlert: Boolean = false
)

data class Plant(
    val id: String,
    val userId: Long = 1L,
    val name: String,
    val species: String,
    val plantedDate: String,
    val location: String,
    val humidity: Int,
    val sunPercentage: Int,
    val sunHoursText: String,
    val lightLevelText: String,
    val nutrientsPercentage: Int,
    val npk: NPKValues,
    val status: PlantStatus,
    val iconType: PlantIconType = PlantIconType.GENERIC,
    val photoUri: String? = null,
    val arduinoId: String = "ARD-01",
    val sensorChannel: String = "Canal A1",
    val isActive: Boolean = true,
    val retiredReason: String? = null,
    val remoteId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis(),
    val recentActivities: List<PlantActivity> = emptyList()
)

data class HuertoAlert(
    val id: String,
    val severity: AlertSeverity,
    val title: String,
    val plantId: String,
    val plantName: String,
    val description: String,
    val timeText: String,
    val isResolved: Boolean = false,
    val resolvedTimeText: String = "Resuelta · Ahora"
)

data class LogEntry(
    val id: String,
    val timeText: String,
    val type: LogType,
    val title: String,
    val description: String,
    val plantId: String? = null,
    val plantName: String? = null,
    val photoUri: String? = null,
    val remoteId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

data class ArduinoDevice(
    val id: String,
    val name: String,
    val model: String,
    val isConnected: Boolean,
    val lastSyncText: String,
    val temperatureCelsius: Int,
    val wifiSignalPercent: Int,
    val assignedUserEmail: String
)

data class WeatherSnapshot(
    val id: Long = 0L,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val ambientHumidity: Int,
    val uvIndex: Double,
    val rainProbability: Int,
    val weatherCode: Int,
    val conditionLabel: String,
    val gardenAdvice: String,
    val fetchedAtText: String,
    val fetchedAtMillis: Long = System.currentTimeMillis(),
    val isFromCache: Boolean = false
)

data class SyncRecord(
    val id: Long = 0L,
    val httpMethod: String,
    val endpoint: String,
    val entityType: String,
    val entityLabel: String,
    val statusCode: Int,
    val success: Boolean,
    val message: String,
    val createdAtText: String
)

sealed class NetworkUiState<out T> {
    object Idle : NetworkUiState<Nothing>()
    object Loading : NetworkUiState<Nothing>()
    data class Success<T>(val data: T) : NetworkUiState<T>()
    data class Error<T>(
        val message: String,
        val cachedData: T? = null,
        val isOffline: Boolean = false
    ) : NetworkUiState<T>()
}

data class CityOption(
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    fun displayLabel(): String =
        listOf(name, region, country).filter { it.isNotBlank() }.distinct().joinToString(", ")
}
