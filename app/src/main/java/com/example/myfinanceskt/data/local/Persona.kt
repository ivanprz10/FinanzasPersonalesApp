package com.example.myfinanceskt.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Alguien con quien compartes una deuda grupal (te debe o le debes).
 */
@Entity(tableName = "personas")
data class Persona(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String
)
