package com.example.myfinanceskt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object. Room genera la implementacion en tiempo de compilacion
 * (por eso necesitamos KSP). Aqui solo declaramos "que" consultas queremos.
 */
@Dao
interface CuentaDao {

    // Devuelve un Flow: la lista se vuelve a emitir sola cuando cambian las cuentas.
    @Query("SELECT * FROM cuentas ORDER BY tipo, nombre")
    fun observarTodas(): Flow<List<Cuenta>>

    // suspend -> se ejecuta en una corrutina, nunca bloquea el hilo principal.
    // Devuelve el id generado por SQLite.
    @Insert
    suspend fun insertar(cuenta: Cuenta): Long
}
