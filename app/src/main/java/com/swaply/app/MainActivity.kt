package com.swaply.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.swaply.app.ui.detail.DetailScreen
import com.swaply.app.ui.detail.DetailViewModel
import com.swaply.app.ui.feed.FeedScreen
import com.swaply.app.ui.feed.FeedViewModel
import com.swaply.app.ui.navigation.Screen
import com.swaply.app.ui.search.SearchScreen
import com.swaply.app.ui.search.SearchViewModel
import com.swaply.app.ui.sell.SellScreen
import com.swaply.app.ui.sell.SellViewModel
import com.swaply.app.ui.theme.SwaplyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SwaplyTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Feed.route,
        Screen.Sell.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Screen.Feed.route,
                        onClick = {
                            navController.navigate(Screen.Feed.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Sell") },
                        label = { Text("Sell") },
                        selected = currentRoute == Screen.Sell.route,
                        onClick = {
                            navController.navigate(Screen.Sell.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                val feedViewModel: FeedViewModel = hiltViewModel()
                FeedScreen(
                    viewModel = feedViewModel,
                    onNavigateToDetail = { productId ->
                        navController.navigate(Screen.Detail.createRoute(productId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            composable(Screen.Search.route) {
                val searchViewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = searchViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { productId ->
                        navController.navigate(Screen.Detail.createRoute(productId))
                    }
                )
            }

            composable(Screen.Sell.route) {
                val sellViewModel: SellViewModel = hiltViewModel()
                SellScreen(
                    viewModel = sellViewModel,
                    onPublishSuccess = { productId ->
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.LongType }
                )
            ) {
                val detailViewModel: DetailViewModel = hiltViewModel()
                DetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
