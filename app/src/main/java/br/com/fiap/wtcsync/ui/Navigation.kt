package br.com.fiap.wtcsync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.com.fiap.wtcsync.ui.auth.EntryScreen
import br.com.fiap.wtcsync.ui.auth.LoginScreen
import br.com.fiap.wtcsync.ui.auth.RegisterScreen
import br.com.fiap.wtcsync.ui.campaigns.CampanhaScreen
import br.com.fiap.wtcsync.ui.chat.ChatListScreen

@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "entry") {
        composable("entry") {
            EntryScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("chatlist") {
                        popUpTo("entry") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("chatlist") {
                        popUpTo("entry") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("chatlist") {
                        popUpTo("entry") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("chatlist") {
            ChatListScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCampanha = { navController.navigate("campanha") }
            )
        }
        composable("campanha") {
            CampanhaScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}