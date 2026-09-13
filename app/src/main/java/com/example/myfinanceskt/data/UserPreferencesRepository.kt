package com.example.myfinanceskt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Instancia UNICA de DataStore para toda la app, ligada al Context.
 *
 * Java -> Kotlin:
 * - Esto es una "propiedad de extension" sobre Context: como agregar un metodo
 *   estatico util a una clase ajena sin heredar de ella.
 * - "by preferencesDataStore(...)" es delegacion: la libreria se encarga de crear
 *   y cachear el objeto la primera vez que se usa.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Capa de acceso al perfil del usuario: nombre + datos de ingresos.
 * Las cuentas/tarjetas NO van aqui: son datos repetibles (varias tarjetas),
 * asi que viven en Room (ver Cuenta.kt / FinanzasRepository).
 */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val TRABAJA = booleanPreferencesKey("trabaja")
        val GANANCIA_NETA = doublePreferencesKey("ganancia_neta")
        val FRECUENCIA_PAGO = stringPreferencesKey("frecuencia_pago")
        val PERFIL_COMPLETO = booleanPreferencesKey("perfil_completo")
    }

    /**
     * Flow = flujo asincrono de valores (parecido a un Publisher reactivo).
     * Emite el nombre guardado y vuelve a emitir cada vez que cambia.
     * Si nunca se ha guardado nada, devuelve "" (cadena vacia).
     */
    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: ""
    }

    val trabaja: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.TRABAJA] ?: false
    }

    val gananciaNeta: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[Keys.GANANCIA_NETA] ?: 0.0
    }

    val frecuenciaPago: Flow<FrecuenciaPago?> = context.dataStore.data.map { prefs ->
        prefs[Keys.FRECUENCIA_PAGO]?.let { FrecuenciaPago.valueOf(it) }
    }

    /**
     * Distingue "onboarding en curso" de "onboarding terminado": el nombre solo
     * no alcanza porque ahora el onboarding tiene varios pasos (nombre, ingresos,
     * tarjetas...) y se puede interrumpir a medias.
     */
    val perfilCompleto: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PERFIL_COMPLETO] ?: false
    }

    /**
     * "suspend" = funcion de corrutina: puede pausarse mientras escribe en disco
     * sin bloquear el hilo principal. Se llama desde un CoroutineScope.
     */
    suspend fun setUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name.trim()
        }
    }

    suspend fun guardarIngresos(trabaja: Boolean, gananciaNeta: Double, frecuenciaPago: FrecuenciaPago?) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TRABAJA] = trabaja
            prefs[Keys.GANANCIA_NETA] = gananciaNeta
            if (frecuenciaPago != null) {
                prefs[Keys.FRECUENCIA_PAGO] = frecuenciaPago.name
            } else {
                prefs.remove(Keys.FRECUENCIA_PAGO)
            }
        }
    }

    suspend fun marcarPerfilCompleto() {
        context.dataStore.edit { prefs ->
            prefs[Keys.PERFIL_COMPLETO] = true
        }
    }
}
