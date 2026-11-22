package com.example.telehuerto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.ui.components.SproutLogoIcon
import com.example.telehuerto.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Andrea") }
    var email by remember { mutableStateOf("andrea@huerto.com") }
    var password by remember { mutableStateOf("huerto2026") }
    var arduinoSerial by remember { mutableStateOf("ARD-01") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBeige)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp)
            ) {
                // Header Row matching Figma Image #1
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

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = if (isRegisterMode) "VINCULACIÓN SEGURA IOT" else "BIENVENIDO DE NUEVO",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isRegisterMode) "Registra tu huerto" else "Cuida lo que crece",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRegisterMode) {
                        "Asocia tu cuenta personal con el número de serie de tu placa Arduino para proteger tus datos."
                    } else {
                        "Ingresa para revisar el estado de tu huerto."
                    },
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isRegisterMode) {
                    Text(
                        text = "Nombre del responsable",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFD5DDD2),
                            focusedContainerColor = Color(0xFFF9FBF7),
                            unfocusedContainerColor = Color(0xFFF9FBF7)
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Text(
                    text = "Correo electrónico",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFD5DDD2),
                        focusedContainerColor = Color(0xFFF9FBF7),
                        unfocusedContainerColor = Color(0xFFF9FBF7)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isRegisterMode) {
                    Text(
                        text = "Código de Serie Arduino (Nodo físico)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = arduinoSerial,
                        onValueChange = {
                            arduinoSerial = it.uppercase()
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color(0xFFD5DDD2),
                            focusedContainerColor = Color(0xFFF9FBF7),
                            unfocusedContainerColor = Color(0xFFF9FBF7)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE2EDE0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔐 El código impreso en tu Arduino (ej. ARD-01) vincula los sensores físicos únicamente a tu cuenta para que nadie más vea tu huerto.",
                            fontSize = 11.sp,
                            color = GreenPrimary,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Contraseña",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color(0xFFD5DDD2),
                        focusedContainerColor = Color(0xFFF9FBF7),
                        unfocusedContainerColor = Color(0xFFF9FBF7)
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AlertCritical
                    )
                }

                if (!isRegisterMode) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { showRecoveryDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val cleanEmail = email.trim()
                        val cleanPass = password.trim()
                        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
                            errorMessage = "Por favor ingresa tu correo y contraseña."
                            return@Button
                        }
                        if (isRegisterMode && arduinoSerial.trim().isEmpty()) {
                            errorMessage = "Ingresa el código de serie de tu placa Arduino (ej. ARD-01)."
                            return@Button
                        }

                        val resolvedName = if (isRegisterMode && name.isNotBlank()) {
                            name.trim()
                        } else {
                            val prefix = cleanEmail.substringBefore("@")
                            if (prefix.equals("andrea", ignoreCase = true)) {
                                "Andrea"
                            } else {
                                prefix.replaceFirstChar { it.uppercase() }
                            }
                        }

                        HuertoRepository.loginUser(email = cleanEmail, name = resolvedName)
                        if (isRegisterMode && arduinoSerial.isNotBlank()) {
                            HuertoRepository.arduinoDevice = HuertoRepository.arduinoDevice.copy(
                                id = arduinoSerial.trim(),
                                assignedUserEmail = cleanEmail
                            )
                        }
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(
                        text = if (isRegisterMode) "Vincular Arduino y entrar   >" else "Ingresar   >",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            isRegisterMode = !isRegisterMode
                            errorMessage = null
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isRegisterMode) "¿Ya tienes un huerto vinculado? " else "¿Aún no tienes un huerto? ",
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
                Text(
                    text = "Recuperación de acceso",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Se ha enviado un enlace de restablecimiento a ${email.ifBlank { "andrea@huerto.com" }}.\n\nPor seguridad, el nodo físico Arduino (${HuertoRepository.arduinoDevice.id}) mantiene cifradas tus lecturas hasta que confirmes tu identidad.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRecoveryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
