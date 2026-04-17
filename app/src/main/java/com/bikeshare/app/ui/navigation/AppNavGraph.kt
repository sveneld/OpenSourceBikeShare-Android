package com.bikeshare.app.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bikeshare.app.R
import com.bikeshare.app.notification.FreeTimeNotificationWorker
import com.bikeshare.app.ui.auth.LoginScreen
import com.bikeshare.app.ui.auth.PhoneVerifyScreen
import com.bikeshare.app.ui.auth.RegisterScreen
import com.bikeshare.app.ui.map.MapScreen
import com.bikeshare.app.ui.rental.RentalScreen
import com.bikeshare.app.ui.profile.ProfileScreen
import com.bikeshare.app.ui.credit.CreditScreen
import com.bikeshare.app.ui.credithistory.CreditHistoryScreen
import com.bikeshare.app.ui.trips.TripsScreen
import com.bikeshare.app.ui.scanner.QrScannerScreen
import com.bikeshare.app.ui.scanner.parseQrScanResult
import com.bikeshare.app.ui.scanner.QrScanResult
import com.bikeshare.app.ui.admin.AdminDashboardScreen
import com.bikeshare.app.ui.admin.stands.AdminStandsScreen
import com.bikeshare.app.ui.admin.bikes.AdminBikesScreen
import com.bikeshare.app.ui.admin.bikes.AdminBikeDetailScreen
import com.bikeshare.app.ui.admin.users.AdminUsersScreen
import com.bikeshare.app.ui.admin.users.AdminUserEditScreen
import com.bikeshare.app.ui.admin.coupons.AdminCouponsScreen
import com.bikeshare.app.ui.admin.reports.AdminReportsScreen

data class BottomNavItem(
    val route: String,
    val icon: @Composable () -> Unit,
    val labelResId: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    appViewModel: AppViewModel = hiltViewModel(),
    navigateTo: String? = null,
    onNavigationConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isAdmin by appViewModel.isAdmin.collectAsStateWithLifecycle(initialValue = false)
    val updateInfo by appViewModel.updateInfo.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(navigateTo) {
        when (navigateTo) {
            FreeTimeNotificationWorker.DESTINATION_RENTALS -> {
                navController.navigate(Screen.Rentals.route) {
                    launchSingleTop = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    restoreState = true
                }
                onNavigationConsumed()
            }
            null -> Unit
            else -> onNavigationConsumed()
        }
    }

    LaunchedEffect(updateInfo) {
        val info = updateInfo ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = context.getString(R.string.update_available, info.latestVersion),
            actionLabel = context.getString(R.string.update_download),
            withDismissAction = true,
            duration = SnackbarDuration.Long,
        )
        if (result == SnackbarResult.ActionPerformed) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(info.releaseUrl))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        appViewModel.dismissUpdate()
    }

    val baseNavItems = listOf(
        BottomNavItem(Screen.Map.route, { Icon(Icons.Default.Map, contentDescription = null) }, R.string.nav_map),
        BottomNavItem(Screen.Rentals.route, { Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null) }, R.string.nav_rentals),
        BottomNavItem(Screen.Profile.route, { Icon(Icons.Default.Person, contentDescription = null) }, R.string.nav_profile),
    )
    val bottomNavItems = if (isAdmin) {
        baseNavItems + BottomNavItem(
            Screen.AdminDashboard.route,
            { Icon(Icons.Default.Settings, contentDescription = null) },
            R.string.nav_admin,
        )
    } else {
        baseNavItems
    }

    val showBottomBar = currentDestination?.route != Screen.Login.route &&
        currentDestination?.route != Screen.Register.route &&
        currentDestination?.route != Screen.PhoneVerify.route &&
        currentDestination?.route != Screen.QrScanner.route

    LaunchedEffect(showBottomBar) {
        if (showBottomBar) appViewModel.loadLimits()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = item.icon,
                            label = { Text(stringResource(item.labelResId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = { phoneConfirmed ->
                        navController.navigate(
                            if (phoneConfirmed) Screen.Map.route else Screen.PhoneVerify.route,
                        ) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.PhoneVerify.route) {
                PhoneVerifyScreen(
                    onVerified = {
                        appViewModel.loadLimits()
                        navController.popBackStack()
                    },
                )
            }

            composable(Screen.Map.route) { backStackEntry ->
                MapScreen(
                    onScanQr = { navController.navigate(Screen.QrScanner.route) },
                    qrSavedStateHandle = backStackEntry.savedStateHandle,
                )
            }

            composable(Screen.Rentals.route) {
                RentalScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToCredit = { navController.navigate(Screen.Credit.route) },
                    onNavigateToCreditHistory = { navController.navigate(Screen.CreditHistory.route) },
                    onNavigateToTrips = { navController.navigate(Screen.Trips.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable(Screen.Credit.route) {
                CreditScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.CreditHistory.route) {
                CreditHistoryScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Trips.route) {
                TripsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.QrScanner.route) {
                QrScannerScreen(
                    onBack = { navController.popBackStack() },
                    onQrDetected = { code ->
                        val mapEntry = navController.getBackStackEntry(Screen.Map.route)
                        val handle = mapEntry.savedStateHandle
                        when (val result = parseQrScanResult(code)) {
                            is QrScanResult.Rent -> {
                                handle?.set("qr_action", "rent")
                                handle?.set("qr_bike_number", result.bikeNumber)
                            }
                            is QrScanResult.Return -> {
                                handle?.set("qr_action", "return")
                                handle?.set("qr_stand_name", result.standName)
                            }
                            QrScanResult.Unknown -> {
                                handle?.set("snackbar", "Unknown QR: $code")
                            }
                        }
                        navController.popBackStack()
                    },
                )
            }

            // Admin screens
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onNavigateToStands = { navController.navigate(Screen.AdminStands.route) },
                    onNavigateToBikes = { navController.navigate(Screen.AdminBikes.route) },
                    onNavigateToUsers = { navController.navigate(Screen.AdminUsers.route) },
                    onNavigateToCoupons = { navController.navigate(Screen.AdminCoupons.route) },
                    onNavigateToReports = { navController.navigate(Screen.AdminReports.route) },
                )
            }

            composable(Screen.AdminStands.route) {
                AdminStandsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.AdminBikes.route) {
                AdminBikesScreen(
                    onBack = { navController.popBackStack() },
                    onBikeClick = { bikeNumber ->
                        navController.navigate(Screen.AdminBikeDetail.createRoute(bikeNumber))
                    },
                )
            }

            composable(
                route = Screen.AdminBikeDetail.route,
                arguments = listOf(navArgument("bikeNumber") { type = NavType.IntType }),
            ) {
                AdminBikeDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.AdminUsers.route) {
                AdminUsersScreen(
                    onBack = { navController.popBackStack() },
                    onUserClick = { userId ->
                        navController.navigate(Screen.AdminUserEdit.createRoute(userId))
                    },
                )
            }

            composable(
                route = Screen.AdminUserEdit.route,
                arguments = listOf(navArgument("userId") { type = NavType.IntType }),
            ) {
                AdminUserEditScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.AdminCoupons.route) {
                AdminCouponsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.AdminReports.route) {
                AdminReportsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
