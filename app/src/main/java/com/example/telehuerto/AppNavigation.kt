package com.example.telehuerto

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.ui.screens.AddPlantScreen
import com.example.telehuerto.ui.screens.AlertsScreen
import com.example.telehuerto.ui.screens.DashboardScreen
import com.example.telehuerto.ui.screens.LogbookScreen
import com.example.telehuerto.ui.screens.LoginScreen
import com.example.telehuerto.ui.screens.PlantDetailScreen
import com.example.telehuerto.ui.screens.PlantsListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(navController)
        }
        composable("plants") {
            PlantsListScreen(navController)
        }
        composable("plant_detail") {
            PlantDetailScreen(navController)
        }
        composable(
            route = "plant_detail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("plantId")?.let { id ->
                HuertoRepository.selectPlant(id)
            }
            PlantDetailScreen(navController)
        }
        composable("add_plant") {
            AddPlantScreen(navController)
        }
        composable("alerts") {
            AlertsScreen(navController)
        }
        composable("logbook") {
            LogbookScreen(navController)
        }
    }
}
