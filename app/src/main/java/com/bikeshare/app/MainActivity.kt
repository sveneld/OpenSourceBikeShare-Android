package com.bikeshare.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.bikeshare.app.notification.FreeTimeNotificationWorker
import com.bikeshare.app.ui.navigation.AppNavGraph
import com.bikeshare.app.ui.theme.BikeShareTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private var pendingNavigation by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        pendingNavigation = intent.getStringExtra(FreeTimeNotificationWorker.EXTRA_NAVIGATE_TO)
        setContent {
            BikeShareTheme {
                AppNavGraph(
                    navigateTo = pendingNavigation,
                    onNavigationConsumed = { pendingNavigation = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(FreeTimeNotificationWorker.EXTRA_NAVIGATE_TO)?.let {
            pendingNavigation = it
        }
    }
}
