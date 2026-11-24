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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.ui.components.SproutLogoIcon
import com.example.telehuerto.ui.theme.AlertCritical
import com.example.telehuerto.ui.theme.AlertCriticalBg
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.BorderSubtle
import com.example.telehuerto.ui.theme.CardBackground
import com.example.telehuerto.ui.theme.DividerColor
import com.example.telehuerto.ui.theme.GreenDark
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.GreenSoft
import com.example.telehuerto.ui.theme.SurfaceTinted
import com.example.telehuerto.ui.theme.TextOnDark
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary
import com.example.telehuerto.util.Validators
import java.util.Locale

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val focusManager = LocalFocusManager.current

    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("Andrea") }
    var email by rememberSaveable { mutableStateOf("andrea@huerto.com") }
    var password by rememberSaveable { mutableStateOf("huerto2026") }
    var arduinoSerial by rememberSaveable { mutableStateOf("ARD-01") }
    var city by rememberSaveable { mutableStateOf("Ciudad de México") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var nameError by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf("") }
    var serialError by rememberSaveable { mutableStateOf("") }
    var cityError by rememberSaveable { mutableStateOf("") }
    var generalError by rememberSaveable { mutableStateOf("") }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var showRecoveryDialog by rememberSaveable { mutableStateOf(false) }

    fun clearErrors() {
        nameError = ""
        emailError = ""
        passwordError = ""
        serialError = ""
        cityError = ""
        generalError = ""
    }

    fun submitForm() {
        if (isLoading) return
        focusManager.clearFocus()
        generalError = ""

        val emailCheck = Validators.validateEmail(email)
        val passCheck = Validators.validatePassword(password)
        emailError = if (emailCheck.isValid) "" else (emailCheck.errorMessage ?: "Correo no válido.")
        passwordError = if (passCheck.isValid) "" else (passCheck.errorMessage ?: "Contraseña no válida.")

        if (isRegisterMode) {
            val nameCheck = Validators.validateName(name)
            val serialCheck = Validators.validateArduinoSerial(arduinoSerial)
            val cityCheck = Validators.validateCityName(city)

            nameError = if (nameCheck.isValid) "" else (nameCheck.errorMessage ?: "Nombre no válido.")
            serialError = if (serialCheck.isValid) "" else (serialCheck.errorMessage ?: "Serie no válida.")
            cityError = if (cityCheck.isValid) "" else (cityCheck.errorMessage ?: "Ciudad no válida.")

            if (!nameCheck.isValid || !emailCheck.isValid || !passCheck.isValid || !serialCheck.isValid || !cityCheck.isValid) {
                return
            }

            isLoading = true
            HuertoRepository.register(
                name = name.trim(),
                email = email.trim(),
                password = password,
                arduinoSerial = arduinoSerial.trim().uppercase(Locale.ROOT),
                city = city.trim()
            ) { result ->
                isLoading = false
                result.fold(
                    onSuccess = { onLoginSuccess() },
                    onFailure = { err ->
                        generalError = err.message ?: "No se pudo completar el registro."
                    }
                )
            }
        } else {
            if (!emailCheck.isValid || !passCheck.isValid) {
                return
            }
            isLoading = true
            HuertoRepository.login(
                email = email.trim(),
                password = password
            ) { result ->
                isLoading = false
                result.fold(
                    onSuccess = { onLoginSuccess() },
                    onFailure = { err ->
                        generalError = err.message ?: "Correo o contraseña incorrectos."
                    }
                )
            }
        }
    }

    fun submitDemoAccess() {
        if (isLoading) return
        focusManager.clearFocus()
        clearErrors()
        email = "andrea@huerto.com"
        password = "huerto2026"
        isLoading = true
        HuertoRepository.loginDemo { result ->
            isLoading = false
            result.fold(
                onSuccess = { onLoginSuccess() },
                onFailure = { err ->
                    generalError = err.message ?: "No se pudo iniciar la cuenta demo."
                }
            )
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = BorderSubtle,
        errorBorderColor = AlertCritical,
        focusedContainerColor = SurfaceTinted,
        unfocusedContainerColor = SurfaceTinted,
        errorContainerColor = AlertCriticalBg.copy(alpha = 0.35f),
        focusedLabelColor = GreenPrimary,
        unfocusedLabelColor = TextSecondary
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBeige)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, BorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                // Identidad de marca Natura
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SproutLogoIcon(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "HUERTO URBANO",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Natura",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isRegisterMode) "NUEVA CUENTA LOCAL" else "BIENVENIDO DE NUEVO",
                    fontSize = 10.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRegisterMode) "Registra tu huerto" else "Cuida lo que crece",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 30.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRegisterMode) {
                        "Crea tu perfil en SQLite, elige tu ciudad para el pronóstico climático y vincula tu nodo de sensores."
                    } else {
                        "Ingresa con tu cuenta para consultar tus plantas, alertas y clima en tiempo real."
                    },
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (isRegisterMode) {
                    Text(
                        text = "Nombre del responsable",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            generalError = ""
                            if (nameError.isNotEmpty()) {
                                val res = Validators.validateName(it)
                                nameError = if (res.isValid) "" else (res.errorMessage ?: "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Ej. Andrea Morales", fontSize = 13.sp, color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.PersonOutline,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = nameError.isNotEmpty(),
                        supportingText = if (nameError.isNotEmpty()) {
                            { Text(nameError, color = AlertCritical, fontSize = 11.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Correo electrónico",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        generalError = ""
                        if (emailError.isNotEmpty()) {
                            val res = Validators.validateEmail(it)
                            emailError = if (res.isValid) "" else (res.errorMessage ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("andrea@huerto.com", fontSize = 13.sp, color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    isError = emailError.isNotEmpty(),
                    supportingText = if (emailError.isNotEmpty()) {
                        { Text(emailError, color = AlertCritical, fontSize = 11.sp) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isRegisterMode) {
                    Text(
                        text = "Ciudad del huerto (clima Open-Meteo)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = city,
                        onValueChange = {
                            city = it
                            generalError = ""
                            if (cityError.isNotEmpty()) {
                                val res = Validators.validateCityName(it)
                                cityError = if (res.isValid) "" else (res.errorMessage ?: "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Ej. Ciudad de México", fontSize = 13.sp, color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = cityError.isNotEmpty(),
                        supportingText = if (cityError.isNotEmpty()) {
                            { Text(cityError, color = AlertCritical, fontSize = 11.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Código de serie del nodo (simulado o físico)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = arduinoSerial,
                        onValueChange = {
                            arduinoSerial = it.uppercase(Locale.ROOT)
                            generalError = ""
                            if (serialError.isNotEmpty()) {
                                val res = Validators.validateArduinoSerial(arduinoSerial)
                                serialError = if (res.isValid) "" else (res.errorMessage ?: "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("ARD-01", fontSize = 13.sp, color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Memory,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = serialError.isNotEmpty(),
                        supportingText = if (serialError.isNotEmpty()) {
                            { Text(serialError, color = AlertCritical, fontSize = 11.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GreenSoft.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "El código de serie (ej. ARD-01) identifica tu nodo de sensores y mantiene tus lecturas separadas por cuenta.",
                                fontSize = 11.sp,
                                color = GreenDark,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "Contraseña",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        generalError = ""
                        if (passwordError.isNotEmpty()) {
                            val res = Validators.validatePassword(it)
                            passwordError = if (res.isValid) "" else (res.errorMessage ?: "")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Mínimo 6 caracteres", fontSize = 13.sp, color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                },
                                tint = TextSecondary
                            )
                        }
                    },
                    isError = passwordError.isNotEmpty(),
                    supportingText = if (passwordError.isNotEmpty()) {
                        { Text(passwordError, color = AlertCritical, fontSize = 11.sp) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitForm() }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors
                )

                if (generalError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AlertCriticalBg,
                        border = BorderStroke(1.dp, AlertCritical.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = AlertCritical,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = generalError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AlertCritical,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                if (!isRegisterMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenPrimary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRecoveryDialog = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { submitForm() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = TextOnDark
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = TextOnDark
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Verificando...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = if (isRegisterMode) "Crear cuenta y vincular" else "Ingresar a mi huerto",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón secundario de acceso rápido demo (Andrea)
                OutlinedButton(
                    onClick = { submitDemoAccess() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceTinted,
                        contentColor = GreenPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Acceso rápido demo (Andrea)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Text(
                                text = "andrea@huerto.com · huerto2026",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            isRegisterMode = !isRegisterMode
                            clearErrors()
                            if (isRegisterMode && email.equals("andrea@huerto.com", ignoreCase = true)) {
                                name = ""
                                email = ""
                                password = ""
                            } else if (!isRegisterMode && email.isBlank()) {
                                name = "Andrea"
                                email = "andrea@huerto.com"
                                password = "huerto2026"
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes un huerto vinculado? " else "¿Aún no tienes una cuenta? ",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isRegisterMode) "Iniciar sesión" else "Crear una cuenta",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }
        }
    }

    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recuperación y cuenta demo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tus cuentas se almacenan de forma local en SQLite con contraseña protegida mediante hash SHA-256 y vinculadas al número de serie del nodo (${HuertoRepository.arduinoDevice.id}).",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceTinted,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Credenciales de demostración",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Correo: andrea@huerto.com\nContraseña: huerto2026\nNodo: ARD-01 · Ciudad de México",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isRegisterMode = false
                        email = "andrea@huerto.com"
                        password = "huerto2026"
                        clearErrors()
                        showRecoveryDialog = false
                    }
                ) {
                    Text(
                        text = "Rellenar demo",
                        color = GreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRecoveryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", color = TextOnDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
