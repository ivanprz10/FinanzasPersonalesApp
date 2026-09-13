package com.example.myfinanceskt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myfinanceskt.data.FinanzasRepository
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

    fun agregarCuenta(nombre: String, tipo: TipoCuenta) {
        viewModelScope.launch { repository.agregarCuenta(nombre, tipo) }
    }

    fun registrarGasto(monto: Double, concepto: String, cuentaId: Long) {
        viewModelScope.launch { repository.registrarGasto(monto, concepto, cuentaId) }
    }

    fun registrarDeuda(persona: String, monto: Double, concepto: String, tipo: TipoDeuda) {
        viewModelScope.launch { repository.registrarDeuda(persona, monto, concepto, tipo) }
    }

    fun marcarDeudaSaldada(deudaId: Long) {
        viewModelScope.launch { repository.marcarDeudaSaldada(deudaId) }
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
