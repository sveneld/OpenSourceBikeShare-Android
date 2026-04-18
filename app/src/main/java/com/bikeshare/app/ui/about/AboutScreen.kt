package com.bikeshare.app.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.bikeshare.app.BuildConfig
import com.bikeshare.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    viewModel: AboutViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (BuildConfig.LOGO_URL.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(BuildConfig.LOGO_URL)
                        .decoderFactory(SvgDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = BuildConfig.APP_NAME,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.about_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            UpdateStatusCard(
                isChecking = uiState.isChecking,
                updateInfo = uiState.updateInfo,
                onDownload = { url -> openUrl(context, url) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinkCard(
                icon = Icons.Default.Language,
                title = stringResource(R.string.about_website),
                subtitle = BuildConfig.WEBSITE_URL,
                onClick = { openUrl(context, BuildConfig.WEBSITE_URL) },
            )
        }
    }
}

@Composable
private fun UpdateStatusCard(
    isChecking: Boolean,
    updateInfo: com.bikeshare.app.domain.update.UpdateInfo?,
    onDownload: (String) -> Unit,
) {
    when {
        isChecking -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.about_checking_update))
                }
            }
        }
        updateInfo != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.update_available, updateInfo.latestVersion),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onDownload(updateInfo.releaseUrl) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.update_download))
                    }
                }
            }
        }
        else -> {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(stringResource(R.string.about_up_to_date))
                }
            }
        }
    }
}

@Composable
private fun LinkCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
