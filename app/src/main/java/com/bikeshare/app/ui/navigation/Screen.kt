package com.bikeshare.app.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object PhoneVerify : Screen("phone_verify")
    data object Map : Screen("map")
    data object Rentals : Screen("rentals")
    data object Profile : Screen("profile")
    data object Credit : Screen("credit")
    data object CreditHistory : Screen("credit_history")
    data object Trips : Screen("trips")
    data object QrScanner : Screen("qr_scanner")
    data object About : Screen("about")

    // Admin
    data object AdminDashboard : Screen("admin/dashboard")
    data object AdminStands : Screen("admin/stands")
    data object AdminBikes : Screen("admin/bikes")
    data object AdminBikeDetail : Screen("admin/bikes/{bikeNumber}") {
        fun createRoute(bikeNumber: Int) = "admin/bikes/$bikeNumber"
    }
    data object AdminUsers : Screen("admin/users")
    data object AdminUserEdit : Screen("admin/users/{userId}") {
        fun createRoute(userId: Int) = "admin/users/$userId"
    }
    data object AdminCoupons : Screen("admin/coupons")
    data object AdminReports : Screen("admin/reports")
}
