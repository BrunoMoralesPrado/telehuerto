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

enum class PlantIconType { TOMATE, ALBAHACA, PIMIENTO, GENERIC }

data class NPKValues(
    val n: Int,
    val p: Int,
    val k: Int,
    val statusLabel: String = "Bajos"
)

data class PlantActivity(
    val title: String,
    val time: String,
    val isAlert: Boolean = false
)

data class Plant(
    val id: String,
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
    val arduinoId: String = "ARD-01",
    val sensorChannel: String = "Canal A1",
    val isActive: Boolean = true,
    val retiredReason: String? = null,
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
    val plantName: String? = null
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
