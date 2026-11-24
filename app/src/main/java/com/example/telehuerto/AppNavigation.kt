package com.example.telehuerto

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.telehuerto.data.HuertoRepository
import com.example.telehuerto.ui.components.SproutLogoIcon
import com.example.telehuerto.ui.screens.AddPlantScreen
import com.example.telehuerto.ui.screens.AlertsScreen
import com.example.telehuerto.ui.screens.DashboardScreen
import com.example.telehuerto.ui.screens.LogbookScreen
import com.example.telehuerto.ui.screens.LoginScreen
import com.example.telehuerto.ui.screens.PlantDetailScreen
import com.example.telehuerto.ui.screens.PlantsListScreen
import com.example.telehuerto.ui.theme.BackgroundBeige
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.TextPrimary
import com.example.telehuerto.ui.theme.TextSecondary

@Composable
fun AppNavigation() {
    val isInitialized = HuertoRepository.isInitialized
    val isLoggedIn = HuertoRepository.isLoggedIn

    if (!isInitialized) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBeige),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SproutLogoIcon(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Natura",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cargando tu huerto inteligente...",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    color = GreenPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        return
    }

    val navController = rememberNavController()
    val startDestination = remember {
        if (HuertoRepository.isLoggedIn) "dashboard" else "login"
    }

    LaunchedEffect(isLoggedIn) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        if (!isLoggedIn && currentRoute != null && currentRoute != "login") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
            PlantDetailScreen(
                navController = navController,
                plantId = null
            )
        }
        composable(
            route = "plant_detail/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId")
            if (!plantId.isNullOrBlank()) {
                HuertoRepository.selectPlant(plantId)
            }
            PlantDetailScreen(
                navController = navController,
                plantId = plantId
            )
        }
        composable("add_plant") {
            AddPlantScreen(
                navController = navController,
                editPlantId = null
            )
        }
        composable(
            route = "edit_plant/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId")
            AddPlantScreen(
                navController = navController,
                editPlantId = plantId
            )
        }
        composable("alerts") {
            AlertsScreen(navController)
        }
        composable("logbook") {
            LogbookScreen(navController)
        }
    }
}
