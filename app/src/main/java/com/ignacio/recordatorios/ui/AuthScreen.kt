package com.ignacio.recordatorios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(vm: AuthViewModel, error: String? = null) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var register by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Recordatorios · Cuenta", style = MaterialTheme.typography.headlineSmall)
        Text(if (register) "Crear cuenta" else "Iniciar sesión")
        OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        if (busy) CircularProgressIndicator()
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        Button(onClick = { vm.login(email, password, register) }, enabled = !busy && email.isNotBlank() && password.length >= 6) {
            Text(if(register) "Registrarse" else "Ingresar")
        }
        TextButton(onClick = { register = !register }) { Text(if(register) "Ya tengo cuenta" else "Crear una cuenta") }
    }
}
