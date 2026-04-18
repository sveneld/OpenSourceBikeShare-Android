package com.bikeshare.app.ui.admin.coupons

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bikeshare.app.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCouponsScreen(
    onBack: () -> Unit,
    viewModel: AdminCouponsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showGenerateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(R.string.admin_coupon_copied)

    LaunchedEffect(uiState.message) { uiState.message?.let { snackbarHostState.showSnackbar(it) } }
    LaunchedEffect(uiState.error) { uiState.error?.let { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_coupons)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showGenerateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.admin_generate))
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading && uiState.coupons.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.coupons) { coupon ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    copyToClipboard(context, coupon.coupon)
                                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(coupon.coupon, style = MaterialTheme.typography.titleMedium)
                                    coupon.value?.let {
                                        Text(
                                            stringResource(R.string.admin_value, it.toString()),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.admin_coupon_copy),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.sellCoupon(coupon.coupon) }) {
                                    Icon(Icons.Default.Sell, contentDescription = stringResource(R.string.admin_mark_sold))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGenerateDialog) {
        var multiplier by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showGenerateDialog = false },
            title = { Text(stringResource(R.string.admin_generate_coupons)) },
            text = {
                OutlinedTextField(
                    value = multiplier,
                    onValueChange = { if (it.all { c -> c.isDigit() }) multiplier = it },
                    label = { Text(stringResource(R.string.admin_multiplier)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(onClick = {
                    multiplier.toIntOrNull()?.let { viewModel.generateCoupons(it) }
                    showGenerateDialog = false
                }) { Text(stringResource(R.string.admin_generate)) }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

private fun copyToClipboard(context: Context, value: String) {
    val clipboard = context.getSystemService<ClipboardManager>() ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("coupon", value))
}
