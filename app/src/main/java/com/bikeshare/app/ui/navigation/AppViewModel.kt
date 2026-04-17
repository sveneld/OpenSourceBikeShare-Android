package com.bikeshare.app.ui.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikeshare.app.domain.repository.RentalRepository
import com.bikeshare.app.domain.update.UpdateChecker
import com.bikeshare.app.domain.update.UpdateInfo
import com.bikeshare.app.notification.FreeTimeNotificationScheduler
import com.bikeshare.app.util.NetworkResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val rentalRepository: RentalRepository,
    private val updateChecker: UpdateChecker,
) : ViewModel() {

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    fun checkForUpdate() {
        viewModelScope.launch {
            val info = updateChecker.checkForUpdate() ?: return@launch
            _updateInfo.value = info
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }

    fun loadLimits() {
        viewModelScope.launch {
            when (val result = rentalRepository.getMyLimits()) {
                is NetworkResult.Success -> {
                    _isAdmin.value = (result.data.privileges ?: 0) >= 1
                    scheduleFreeTimeNotifications(result.data.freeTimeMinutes ?: 30)
                }
                is NetworkResult.Error -> {
                    _isAdmin.value = false
                    FreeTimeNotificationScheduler.cancelAll(appContext)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private suspend fun scheduleFreeTimeNotifications(freeTimeMinutes: Int) {
        when (val bikesResult = rentalRepository.getMyBikes()) {
            is NetworkResult.Success -> {
                val bikes = bikesResult.data
                if (bikes.isEmpty()) {
                    FreeTimeNotificationScheduler.cancelAll(appContext)
                } else {
                    bikes.forEach { bike ->
                        val rentedSec = bike.rentedSeconds ?: 0
                        FreeTimeNotificationScheduler.schedule(
                            appContext,
                            bike.bikeNum,
                            rentedSec,
                            freeTimeMinutes,
                        )
                    }
                }
            }
            is NetworkResult.Error -> FreeTimeNotificationScheduler.cancelAll(appContext)
            is NetworkResult.Loading -> {}
        }
    }
}
