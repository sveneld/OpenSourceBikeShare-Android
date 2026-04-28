package com.bikeshare.app.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeshare.app.data.api.dto.BikeOnStandDto
import com.bikeshare.app.data.api.dto.RentedBikeDto
import com.bikeshare.app.data.api.dto.RentSystemResultDto
import com.bikeshare.app.data.api.dto.StandMarkerDto
import com.bikeshare.app.data.api.dto.UserLimitsDto
import com.bikeshare.app.domain.repository.RentalRepository
import com.bikeshare.app.domain.repository.StandRepository
import com.bikeshare.app.notification.FreeTimeNotificationScheduler
import com.bikeshare.app.util.NetworkResult
import com.bikeshare.app.util.RentMessageRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val stands: List<StandMarkerDto> = emptyList(),
    val selectedStand: StandMarkerDto? = null,
    val standBikes: List<BikeOnStandDto> = emptyList(),
    val myBikes: List<RentedBikeDto> = emptyList(),
    val limits: UserLimitsDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val rentResult: String? = null,
    val rentCodeInfo: RentSystemResultDto? = null,
    val rentCodeMessage: String? = null,
    val returnResult: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val standRepository: StandRepository,
    private val rentalRepository: RentalRepository,
    private val messageRenderer: RentMessageRenderer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        loadStandMarkers()
        loadMyBikes()
        loadLimits()
    }

    fun loadStandMarkers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = standRepository.getMarkers()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        stands = result.data,
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

    fun selectStand(stand: StandMarkerDto) {
        _uiState.value = _uiState.value.copy(selectedStand = stand, standBikes = emptyList())
        loadStandBikes(stand.standName)
    }

    fun clearSelectedStand() {
        _uiState.value = _uiState.value.copy(selectedStand = null, standBikes = emptyList())
    }

    private fun loadMyBikes() {
        viewModelScope.launch {
            when (val result = rentalRepository.getMyBikes()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(myBikes = result.data)
                }
                is NetworkResult.Error -> Timber.w("Failed to load my bikes: ${result.message}")
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadLimits() {
        viewModelScope.launch {
            when (val result = rentalRepository.getMyLimits()) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(limits = result.data)
                }
                is NetworkResult.Error -> Timber.w("Failed to load limits: ${result.message}")
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadStandBikes(standName: String) {
        viewModelScope.launch {
            when (val result = standRepository.getBikes(standName)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(standBikes = result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun rentBike(bikeNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = rentalRepository.rentBike(bikeNumber)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        rentCodeInfo = result.data,
                        rentCodeMessage = messageRenderer.renderFromDto(
                            result.data.code,
                            result.data.params,
                            fallback = result.data.message,
                        ),
                    )
                    _uiState.value.selectedStand?.let { loadStandBikes(it.standName) }
                    loadStandMarkers()
                    loadMyBikes()
                    loadLimits()
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

    fun returnBike(bikeNumber: Int, standName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = rentalRepository.returnBike(bikeNumber, standName, null)) {
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
                    _uiState.value.selectedStand?.let { loadStandBikes(it.standName) }
                    loadStandMarkers()
                    loadMyBikes()
                    loadLimits()
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

    fun clearRentResult() {
        _uiState.value = _uiState.value.copy(rentResult = null)
    }

    fun clearRentCodeInfo() {
        _uiState.value = _uiState.value.copy(rentCodeInfo = null, rentCodeMessage = null)
    }

    fun clearReturnResult() {
        _uiState.value = _uiState.value.copy(returnResult = null)
    }
}
