package br.com.fiap.wtcsync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
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
import androidx.navigation.navArgument
import br.com.fiap.wtcsync.ui.campaigns.CampanhaScreen
import br.com.fiap.wtcsync.ui.messages.MessageScreen
import br.com.fiap.wtcsync.ui.crm.ChatListScreen


@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chatlist") {

        // Tela de lista de chats
        composable("chatlist") {
            ChatListScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCampanha = { navController.navigate("campanha") },
                onChatClick = { cliente ->
                    navController.navigate("message_screen/${cliente.nome}")
                }
            )
        }
        composable("campanha") {
            CampanhaScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Tela de mensagens
        composable(
            route = "message_screen/{contactName}",
            arguments = listOf(navArgument("contactName") { type = NavType.StringType })
        ) { backStackEntry ->

            val contactName = backStackEntry.arguments?.getString("contactName") ?: "Contato"

            MessageScreen(
                contactName = contactName,
                navController = navController
            )
        }
    }
}