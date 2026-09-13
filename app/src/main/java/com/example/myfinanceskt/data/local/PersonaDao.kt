package com.example.myfinanceskt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonaDao {

    @Query("SELECT * FROM personas ORDER BY nombre")
    fun observarTodas(): Flow<List<Persona>>

    @Query("SELECT * FROM personas WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Persona?

    @Insert
    suspend fun insertar(persona: Persona): Long
}
