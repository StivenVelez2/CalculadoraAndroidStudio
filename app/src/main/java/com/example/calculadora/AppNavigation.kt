package com.example.calculadora

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: CalculatorViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "calculator"
    ) {
        composable("calculator") {
            ShowCalculator(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("history") {
            ShowHistory(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}
