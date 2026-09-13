package com.example.myfinanceskt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * La base de datos. Lista de entidades + version + convertidores.
 *
 * exportSchema = false: no guardamos el JSON del esquema en disco (lo activaremos
 * cuando empecemos a versionar migraciones).
 */
@Database(
    entities = [Cuenta::class, Gasto::class, Persona::class, Deuda::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cuentaDao(): CuentaDao
    abstract fun gastoDao(): GastoDao
    abstract fun personaDao(): PersonaDao
    abstract fun deudaDao(): DeudaDao

    companion object {
        // @Volatile -> los cambios a INSTANCE son visibles de inmediato para todos los hilos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Patron singleton: una sola instancia de la BD en toda la app.
         * "?:" (elvis) -> "si lo de la izquierda es null, usa lo de la derecha".
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myfinances.db"
                )
                    .addCallback(SEED_CALLBACK)
                    // Sin migraciones todavia (app en desarrollo, sin datos que proteger):
                    // si sube la version, recrea la BD en vez de fallar.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        /**
         * Se ejecuta UNA sola vez, cuando la BD se crea por primera vez.
         * Deja creada la cuenta "Efectivo" para que el primer gasto tenga a donde ir.
         */
        private val SEED_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO cuentas (nombre, tipo) VALUES ('Efectivo', 'EFECTIVO')"
                )
            }
        }
    }
}
