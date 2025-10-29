package br.com.fiap.wtcsync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

        // Tela de campanhas
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