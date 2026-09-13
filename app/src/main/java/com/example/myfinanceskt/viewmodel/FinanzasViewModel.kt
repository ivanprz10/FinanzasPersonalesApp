package com.example.myfinanceskt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myfinanceskt.data.FinanzasRepository
import com.example.myfinanceskt.data.local.Categoria
import com.example.myfinanceskt.data.local.CompraPlazosConCuenta
import com.example.myfinanceskt.data.local.Cuenta
import com.example.myfinanceskt.data.local.DeudaConPersona
import com.example.myfinanceskt.data.local.GastoConCuenta
import com.example.myfinanceskt.data.local.TipoCuenta
import com.example.myfinanceskt.data.local.TipoDeuda
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Unico punto de acceso a FinanzasRepository para la UI.
 *
 * Saca de las pantallas dos cosas que no son su responsabilidad:
 * 1) manejar corrutinas para escribir en Room (viewModelScope.launch),
 * 2) calcular numeros derivados (ej. saldoNetoGrupos).
 *
 * "stateIn" convierte cada Flow de Room en un StateFlow: guarda el ultimo
 * valor emitido, asi que sobrevive a una recomposicion sin re-consultar la BD.
 * "WhileSubscribed(5_000)" mantiene la coleccion activa 5s despues de que la
 * pantalla deje de observar, para no relanzar la query si solo fue un giro rapido.
 */
class FinanzasViewModel(private val repository: FinanzasRepository) : ViewModel() {

    private val comparteEstado = SharingStarted.WhileSubscribed(5_000)

    val cuentas: StateFlow<List<Cuenta>> =
        repository.cuentas.stateIn(viewModelScope, comparteEstado, emptyList())

    val gastos: StateFlow<List<GastoConCuenta>> =
        repository.gastos.stateIn(viewModelScope, comparteEstado, emptyList())

    val totalGastado: StateFlow<Double> =
        repository.totalGastado.stateIn(viewModelScope, comparteEstado, 0.0)

    val deudasActivas: StateFlow<List<DeudaConPersona>> =
        repository.deudasActivas.stateIn(viewModelScope, comparteEstado, emptyList())

    val totalTeDeben: StateFlow<Double> =
        repository.totalTeDeben.stateIn(viewModelScope, comparteEstado, 0.0)

    val totalDebo: StateFlow<Double> =
        repository.totalDebo.stateIn(viewModelScope, comparteEstado, 0.0)

    /**
     * Saldo neto de las deudas grupales: positivo = a tu favor en total,
     * negativo = debes mas de lo que te deben. Es el tipo de calculo que
     * le corresponde al backend, no a la pantalla.
     */
    val saldoNetoGrupos: StateFlow<Double> =
        combine(totalTeDeben, totalDebo) { teDeben, debo -> teDeben - debo }
            .stateIn(viewModelScope, comparteEstado, 0.0)

    val comprasPlazosActivas: StateFlow<List<CompraPlazosConCuenta>> =
        repository.comprasPlazosActivas.stateIn(viewModelScope, comparteEstado, emptyList())

    val cuotaMensualTotalPlazos: StateFlow<Double> =
        repository.cuotaMensualTotalPlazos.stateIn(viewModelScope, comparteEstado, 0.0)

    fun agregarCuenta(
        nombre: String,
        tipo: TipoCuenta,
        diaCorte: Int? = null,
        diasParaPago: Int? = null,
        tieneComprasAMeses: Boolean? = null
    ) {
        viewModelScope.launch {
            repository.agregarCuenta(nombre, tipo, diaCorte, diasParaPago, tieneComprasAMeses)
        }
    }

    fun registrarGasto(
        monto: Double,
        concepto: String,
        cuentaId: Long,
        categoria: Categoria = Categoria.OTRO,
        emojiPersonalizado: String? = null
    ) {
        viewModelScope.launch {
            repository.registrarGasto(monto, concepto, cuentaId, categoria, emojiPersonalizado)
        }
    }

    /**
     * Usar esta desde "Realice una compra": una sola llamada sin importar si
     * el usuario eligio "a meses" (numeroMeses > 1) o "una sola exhibicion"
     * (numeroMeses null o 1). Ver FinanzasRepository.registrarCompra.
     *
     * "categoria" es la seleccion generica (Comida, Transporte, Gasolina...);
     * si no se elige ninguna, queda Categoria.OTRO. "emojiPersonalizado" es el
     * emoji que el usuario escriba a mano (teclado del celular) cuando ninguna
     * categoria le sirve; si se manda, la UI deberia mostrar ese en vez del
     * emoji fijo de la categoria.
     *
     * "monto" es siempre el monto ORIGINAL (de contado). Si la compra es a
     * plazos y NO es MSI, manda "isMsi = false" y "costoFinanciamiento" con
     * el recargo — la cuota real se calcula sobre (monto + costoFinanciamiento).
     * Si es MSI (default), no hace falta tocar esos dos parametros.
     */
    fun registrarCompra(
        monto: Double,
        concepto: String,
        cuentaId: Long,
        numeroMeses: Int? = null,
        categoria: Categoria = Categoria.OTRO,
        emojiPersonalizado: String? = null,
        isMsi: Boolean = true,
        costoFinanciamiento: Double = 0.0
    ) {
        viewModelScope.launch {
            repository.registrarCompra(
                monto, concepto, cuentaId, numeroMeses, categoria, emojiPersonalizado,
                isMsi, costoFinanciamiento
            )
        }
    }

    fun registrarDeuda(persona: String, monto: Double, concepto: String, tipo: TipoDeuda) {
        viewModelScope.launch { repository.registrarDeuda(persona, monto, concepto, tipo) }
    }

    fun marcarDeudaSaldada(deudaId: Long) {
        viewModelScope.launch { repository.marcarDeudaSaldada(deudaId) }
    }

    fun registrarCompraPlazos(
        cuentaId: Long,
        concepto: String,
        montoOriginal: Double,
        numeroMeses: Int,
        categoria: Categoria = Categoria.OTRO,
        emojiPersonalizado: String? = null,
        isMsi: Boolean = true,
        costoFinanciamiento: Double = 0.0
    ) {
        viewModelScope.launch {
            repository.registrarCompraPlazos(
                cuentaId, concepto, montoOriginal, numeroMeses, categoria, emojiPersonalizado,
                isMsi, costoFinanciamiento
            )
        }
    }

    fun marcarMesPagadoPlazos(compraId: Long) {
        viewModelScope.launch { repository.marcarMesPagadoPlazos(compraId) }
    }
}

/**
 * FinanzasRepository no tiene constructor vacio (necesita los DAOs de Room),
 * asi que el ViewModelProvider por defecto no puede crearlo solo: esta
 * factory le enseña como.
 *
 * Uso en la UI (Activity/Composable, no aqui):
 *   val factory = FinanzasViewModelFactory(finanzas)
 *   val viewModel: FinanzasViewModel = viewModel(factory = factory)
 */
class FinanzasViewModelFactory(
    private val repository: FinanzasRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        FinanzasViewModel(repository) as T
}
