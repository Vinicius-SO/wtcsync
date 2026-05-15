package br.com.fiap.wtcsync.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.fiap.wtcsync.data.model.Campanha
import br.com.fiap.wtcsync.ui.auth.EntryScreen
import br.com.fiap.wtcsync.ui.auth.LoginScreen
import br.com.fiap.wtcsync.ui.auth.RegisterScreen
import br.com.fiap.wtcsync.ui.campaigns.CampanhaCreateScreen
import br.com.fiap.wtcsync.ui.campaigns.CampanhaListScreen
import br.com.fiap.wtcsync.ui.campaigns.CampanhaScreen
import br.com.fiap.wtcsync.ui.components.BottomNavBar
import br.com.fiap.wtcsync.ui.components.BottomNavTab
import br.com.fiap.wtcsync.ui.crm.ChatListScreen
import br.com.fiap.wtcsync.ui.crm.ClientesScreen
import br.com.fiap.wtcsync.ui.crm.SegmentosScreen
import br.com.fiap.wtcsync.ui.messages.MessageScreen

@Composable
fun Navigation(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "home") {
        composable("entry") {
            EntryScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("entry") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("entry") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
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

@Composable
private fun HomeScreen(navController: NavHostController) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.CHAT) }
    var selectedCampanha by remember { mutableStateOf<Campanha?>(null) }
    var creatingCampanha by remember { mutableStateOf(false) }

    when {
        creatingCampanha -> {
            CampanhaCreateScreen(
                onCancelClick = { creatingCampanha = false }
            )
        }
        else -> {
            Scaffold(
                bottomBar = {
                    BottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    when (selectedTab) {
                        BottomNavTab.CLIENTES -> ClientesScreen()
                        BottomNavTab.SEGMENTOS -> SegmentosScreen()
                        BottomNavTab.CAMPANHAS -> {
                            if (selectedCampanha == null) {
                                CampanhaListScreen(
                                    onCampanhaClick = { campanha ->
                                        selectedCampanha = campanha
                                    },
                                    onCreateClick = { creatingCampanha = true }
                                )
                            } else {
                                CampanhaScreen(
                                    campanha = selectedCampanha,
                                    onBackClick = { selectedCampanha = null }
                                )
                            }
                        }
                        BottomNavTab.CHAT -> ChatListScreen(
                            onChatClick = { cliente ->
                                navController.navigate("message_screen/${cliente.nome}")
                            }
                        )
                    }
                }
            }
        }
    }
}
