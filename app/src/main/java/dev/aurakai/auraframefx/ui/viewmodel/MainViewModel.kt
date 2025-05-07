package dev.aurakai.auraframefx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    // Inject your repositories here
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        loadData()
    }


    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = MainUiState.Success("Welcome to AuraFrameFX")
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    data class Success(val message: String) : MainUiState()
    data class Error(val message: String) : MainUiState()
}
