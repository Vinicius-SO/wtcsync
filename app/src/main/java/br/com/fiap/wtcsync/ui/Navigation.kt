package com.example.seuprojeto.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import br.com.fiap.wtcsync.ui.campaigns.CampanhaScreen
import com.example.seuprojeto.ui.chat.ChatListScreen


@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "chatlist") {
        // Tela de lista de chats
        composable("chatlist") {
            ChatListScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToCampanha = { navController.navigate("campanha") } //
            )
        }

        // Tela de campanhas
        composable("campanha") {
            CampanhaScreen(
                onBackClick = { navController.popBackStack() } //
            )
        }
    }
}


