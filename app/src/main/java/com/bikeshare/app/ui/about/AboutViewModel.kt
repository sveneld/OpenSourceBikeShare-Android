package com.bikeshare.app.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeshare.app.domain.update.UpdateChecker
import com.bikeshare.app.domain.update.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AboutUiState(
    val updateInfo: UpdateInfo? = null,
    val isChecking: Boolean = false,
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val updateChecker: UpdateChecker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true)
            // force = true: bypass the 24h throttle so user sees fresh result on demand
            val info = updateChecker.checkForUpdate(force = true)
            _uiState.value = AboutUiState(updateInfo = info, isChecking = false)
        }
    }
}
