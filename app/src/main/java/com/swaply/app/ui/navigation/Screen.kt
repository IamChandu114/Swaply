package com.swaply.app.ui.navigation

sealed class Screen(val route: String) {
    data object Feed : Screen("feed")
    data object Search : Screen("search")
    data object Favorites : Screen("favorites")
    data object Sell : Screen("sell")
    data object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Long) = "detail/$productId"
    }
}
