package com.bikeshare.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bikeshare.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToStands: () -> Unit,
    onNavigateToBikes: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToCoupons: () -> Unit,
    onNavigateToReports: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_admin)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AdminMenuItem(
                icon = Icons.Default.Place,
                title = stringResource(R.string.admin_stands),
                onClick = onNavigateToStands,
            )
            AdminMenuItem(
                icon = Icons.AutoMirrored.Filled.DirectionsBike,
                title = stringResource(R.string.admin_bikes),
                onClick = onNavigateToBikes,
            )
            AdminMenuItem(
                icon = Icons.Default.People,
                title = stringResource(R.string.admin_users),
                onClick = onNavigateToUsers,
            )
            AdminMenuItem(
                icon = Icons.Default.CardGiftcard,
                title = stringResource(R.string.admin_coupons),
                onClick = onNavigateToCoupons,
            )
            AdminMenuItem(
                icon = Icons.Default.Assessment,
                title = stringResource(R.string.admin_reports),
                onClick = onNavigateToReports,
            )
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
