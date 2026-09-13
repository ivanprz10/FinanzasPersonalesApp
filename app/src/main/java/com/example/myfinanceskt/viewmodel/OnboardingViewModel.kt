package com.example.myfinanceskt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myfinanceskt.data.FrecuenciaPago
import com.example.myfinanceskt.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Perfil del usuario: nombre + datos de ingresos, guardados en DataStore.
 * Las cuentas/tarjetas NO viven aqui (son varias, viven en Room): para eso
 * esta FinanzasViewModel.agregarCuenta, que se puede llamar tanto durante
 * el onboarding como despues, desde cualquier pantalla.
 */
class OnboardingViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val comparteEstado = SharingStarted.WhileSubscribed(5_000)

    val userName: StateFlow<String> =
        repository.userName.stateIn(viewModelScope, comparteEstado, "")

    val trabaja: StateFlow<Boolean> =
        repository.trabaja.stateIn(viewModelScope, comparteEstado, false)

    val gananciaNeta: StateFlow<Double> =
        repository.gananciaNeta.stateIn(viewModelScope, comparteEstado, 0.0)

    val frecuenciaPago: StateFlow<FrecuenciaPago?> =
        repository.frecuenciaPago.stateIn(viewModelScope, comparteEstado, null)

    /**
     * Diferencia "todavia sin terminar el onboarding" de "ya lo termino":
     * el nombre ya no basta como senal porque ahora el flujo tiene varios
     * pasos y se puede interrumpir a medias.
     */
    val perfilCompleto: StateFlow<Boolean> =
        repository.perfilCompleto.stateIn(viewModelScope, comparteEstado, false)

    fun guardarNombre(nombre: String) {
        viewModelScope.launch { repository.setUserName(nombre) }
    }

    fun guardarIngresos(trabaja: Boolean, gananciaNeta: Double, frecuenciaPago: FrecuenciaPago?) {
        viewModelScope.launch { repository.guardarIngresos(trabaja, gananciaNeta, frecuenciaPago) }
    }

    /** Llamar al terminar el ultimo paso del onboarding (tarjetas incluidas). */
    fun marcarPerfilCompleto() {
        viewModelScope.launch { repository.marcarPerfilCompleto() }
    }
}

/**
 * UserPreferencesRepository necesita un Context en el constructor, asi que
 * el ViewModelProvider por defecto no puede crear OnboardingViewModel solo.
 *
 * Uso en la UI (Activity/Composable, no aqui):
 *   val factory = OnboardingViewModelFactory(userPrefs)
 *   val viewModel: OnboardingViewModel = viewModel(factory = factory)
 */
class OnboardingViewModelFactory(
    private val repository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OnboardingViewModel(repository) as T
}
