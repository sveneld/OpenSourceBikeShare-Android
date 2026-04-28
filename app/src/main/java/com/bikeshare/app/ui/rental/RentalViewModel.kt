package com.bikeshare.app.ui.rental

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeshare.app.data.api.dto.RentedBikeDto
import com.bikeshare.app.data.api.dto.StandMarkerDto
import com.bikeshare.app.domain.repository.RentalRepository
import com.bikeshare.app.domain.repository.StandRepository
import com.bikeshare.app.notification.FreeTimeNotificationScheduler
import com.bikeshare.app.util.NetworkResult
import com.bikeshare.app.util.RentMessageRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RentalUiState(
    val rentedBikes: List<RentedBikeDto> = emptyList(),
    val stands: List<StandMarkerDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val returnResult: String? = null,
)

@HiltViewModel
class RentalViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val rentalRepository: RentalRepository,
    private val standRepository: StandRepository,
    private val messageRenderer: RentMessageRenderer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalUiState())
    val uiState: StateFlow<RentalUiState> = _uiState

    init {
        loadRentedBikes()
        loadStands()
    }

    fun loadRentedBikes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = rentalRepository.getMyBikes()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        rentedBikes = result.data,
                        isLoading = false,
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false,
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadStands() {
        viewModelScope.launch {
            when (val result = standRepository.getMarkers()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(stands = result.data)
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun returnBike(bikeNumber: Int, standName: String, note: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = rentalRepository.returnBike(bikeNumber, standName, note)) {
                is NetworkResult.Success -> {
                    FreeTimeNotificationScheduler.cancelForBike(appContext, bikeNumber)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        returnResult = messageRenderer.renderFromDto(
                            result.data.code,
                            result.data.params,
                            fallback = result.data.message,
                        ),
                    )
                    loadRentedBikes()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = messageRenderer.render(
                            result.messageCode,
                            result.messageParams,
                            fallback = result.message,
                        ),
                    )
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearReturnResult() {
        _uiState.value = _uiState.value.copy(returnResult = null)
    }
}
