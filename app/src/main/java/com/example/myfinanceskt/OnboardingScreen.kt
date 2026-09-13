package com.example.myfinanceskt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myfinanceskt.ui.theme.MyFinancesKTTheme

/**
 * Pantalla de bienvenida. Se muestra solo la primera vez (mientras no haya
 * un nombre guardado en DataStore).
 *
 * @param onNameConfirmed callback que dispara MainActivity para persistir el nombre.
 *
 * Java -> Kotlin:
 * - "var name by rememberSaveable { mutableStateOf("") }" crea un estado observable:
 *   cuando cambia, Compose vuelve a dibujar lo que dependa de el.
 *   "rememberSaveable" ademas lo conserva si giras la pantalla.
 * - "() -> Unit" es el tipo de una lambda sin parametros que no devuelve nada
 *   (como un Runnable de Java).
 */
@Composable
fun OnboardingScreen(onNameConfirmed: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val isValid = name.trim().isNotEmpty()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido a MyFinances",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tu dinero, 100% en tu telefono. Sin cuentas ni servidores.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "¿Como te llamas?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tu nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onNameConfirmed(name.trim()) },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Empezar")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview() {
    MyFinancesKTTheme {
        OnboardingScreen(onNameConfirmed = {})
    }
}
