package com.bikeshare.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.bikeshare.app.BuildConfig
import com.bikeshare.app.R

@Composable
fun LoginScreen(
    onLoginSuccess: (phoneConfirmed: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var number by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess(uiState.phoneConfirmed)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (BuildConfig.LOGO_URL.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(BuildConfig.LOGO_URL)
                    .decoderFactory(SvgDecoder.Factory())
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text(stringResource(R.string.login_phone_number)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(number, password) },
            enabled = !uiState.isLoading && number.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.login_button))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text(stringResource(R.string.register_link))
        }

        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }

        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }
}
