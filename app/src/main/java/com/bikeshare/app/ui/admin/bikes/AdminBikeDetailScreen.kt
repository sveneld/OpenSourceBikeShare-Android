package com.bikeshare.app.ui.admin.bikes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bikeshare.app.R
import com.bikeshare.app.data.api.dto.BikeDetailDto
import com.bikeshare.app.data.api.dto.BikeLastUsageDto
import com.bikeshare.app.domain.repository.BikeRepository
import com.bikeshare.app.util.NetworkResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.bikeshare.app.data.api.ApiService
import com.bikeshare.app.util.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBikeDetailUiState(
    val bike: BikeDetailDto? = null,
    val lastUsage: BikeLastUsageDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
)

@HiltViewModel
class AdminBikeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: ApiService,
    private val moshi: Moshi,
    private val bikeRepository: BikeRepository,
) : ViewModel() {

    private val bikeNumber: Int = savedStateHandle["bikeNumber"] ?: 0

    private val _uiState = MutableStateFlow(AdminBikeDetailUiState())
    val uiState: StateFlow<AdminBikeDetailUiState> = _uiState

    init {
        loadBike()
        loadLastUsage()
    }

    fun loadBike() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = safeApiCall(moshi) { api.getAdminBike(bikeNumber) }) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(bike = result.data, isLoading = false)
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message, isLoading = false)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun loadLastUsage() {
        viewModelScope.launch {
            when (val result = safeApiCall(moshi) { api.getBikeLastUsage(bikeNumber) }) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(lastUsage = result.data)
                }
                is NetworkResult.Error -> { /* silently ignore */ }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun setLockCode(code: String) {
        viewModelScope.launch {
            when (val result = bikeRepository.setBikeLockCode(bikeNumber, code)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(message = "Lock code updated")
                    loadBike()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun deleteNotes() {
        viewModelScope.launch {
            when (val result = bikeRepository.deleteBikeNotes(bikeNumber)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(message = "Notes deleted")
                    loadBike()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun revertBike() {
        viewModelScope.launch {
            when (val result = bikeRepository.revertBike(bikeNumber)) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(message = "Bike state reverted")
                    loadBike()
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(error = result.message)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBikeDetailScreen(
    onBack: () -> Unit,
    viewModel: AdminBikeDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLockCodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Bike #${uiState.bike?.bikeNum ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.bike?.let { bike ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.bike_number, bike.bikeNum), style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        bike.standName?.let { Text(stringResource(R.string.admin_stand_label, it)) }
                        bike.userName?.let { Text(stringResource(R.string.admin_rented_by, it), color = MaterialTheme.colorScheme.primary) }
                        bike.rentTime?.let { Text(stringResource(R.string.admin_rent_time, it)) }
                        bike.notes?.let {
                            if (it.isNotBlank()) Text(stringResource(R.string.admin_note_label, it), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { showLockCodeDialog = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.admin_set_code))
                    }

                    OutlinedButton(
                        onClick = { viewModel.deleteNotes() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.admin_del_notes))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.revertBike() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_revert_bike_state))
                }

                // Last usage section
                uiState.lastUsage?.let { usage ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.admin_last_usage), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val notes = usage.notes
                    if (notes?.isNotBlank() == true) {
                        Text(stringResource(R.string.admin_notes_label, notes), color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    usage.history.forEach { item ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${item.action ?: ""}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    item.userName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    item.standName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                        ?: item.parameter?.let { Text(stringResource(R.string.admin_code_label, it), style = MaterialTheme.typography.bodySmall) }
                                    item.time?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLockCodeDialog) {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLockCodeDialog = false },
            title = { Text(stringResource(R.string.admin_set_lock_code)) },
            text = {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) code = it },
                    label = { Text(stringResource(R.string.admin_lock_code_hint)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setLockCode(code)
                        showLockCodeDialog = false
                    },
                    enabled = code.length == 4,
                ) { Text(stringResource(R.string.admin_set)) }
            },
            dismissButton = {
                TextButton(onClick = { showLockCodeDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
