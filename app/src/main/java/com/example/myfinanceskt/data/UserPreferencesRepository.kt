package com.example.myfinanceskt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
 * Capa de acceso a datos del usuario (equivalente a un Repository de Spring).
 * De momento solo guarda el nombre; luego crecera (moneda, tema, etc.).
 */
class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
    }

    /**
     * Flow = flujo asincrono de valores (parecido a un Publisher reactivo).
     * Emite el nombre guardado y vuelve a emitir cada vez que cambia.
     * Si nunca se ha guardado nada, devuelve "" (cadena vacia).
     */
    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: ""
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
}
