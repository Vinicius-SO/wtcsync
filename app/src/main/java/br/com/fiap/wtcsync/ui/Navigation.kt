package br.com.fiap.wtcsync.ui

import br.com.fiap.wtcsync.ui.components.*
import android.app.Application
import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import br.com.fiap.wtcsync.theme.LocalReduceMotion
import br.com.fiap.wtcsync.theme.WtcMotion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import br.com.fiap.wtcsync.WtcSyncApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import br.com.fiap.wtcsync.campaigns.ui.create.CreateCampaignScreen
import br.com.fiap.wtcsync.campaigns.ui.create.CreateCampaignViewModel
import br.com.fiap.wtcsync.campaigns.ui.create.CreateCampaignViewModelFactory
import br.com.fiap.wtcsync.campaigns.ui.detail.CampaignDetailScreen
import br.com.fiap.wtcsync.campaigns.ui.detail.CampaignDetailViewModel
import br.com.fiap.wtcsync.campaigns.ui.detail.CampaignDetailViewModelFactory
import br.com.fiap.wtcsync.campaigns.ui.client.ClientCampaignScreen
import br.com.fiap.wtcsync.campaigns.ui.list.CampaignListScreen
import br.com.fiap.wtcsync.campaigns.ui.list.CampaignListViewModel
import br.com.fiap.wtcsync.campaigns.ui.list.CampaignListViewModelFactory
import br.com.fiap.wtcsync.segments.ui.create.CreateSegmentScreen
import br.com.fiap.wtcsync.segments.ui.create.CreateSegmentViewModel
import br.com.fiap.wtcsync.segments.ui.create.CreateSegmentViewModelFactory
import br.com.fiap.wtcsync.ui.auth.AuthViewModel
import br.com.fiap.wtcsync.ui.auth.AuthViewModelFactory
import br.com.fiap.wtcsync.ui.auth.EntryScreen
import br.com.fiap.wtcsync.ui.auth.LoginScreen
import br.com.fiap.wtcsync.ui.auth.RegisterScreen
import br.com.fiap.wtcsync.ui.components.BottomNavBar
import br.com.fiap.wtcsync.ui.components.BottomNavTab
import br.com.fiap.wtcsync.ui.crm.ChatListScreen
import br.com.fiap.wtcsync.ui.crm.ClientFormScreen
import br.com.fiap.wtcsync.ui.crm.ClientesScreen
import br.com.fiap.wtcsync.ui.crm.SegmentosScreen
import br.com.fiap.wtcsync.ui.messages.MessageScreen
import br.com.fiap.wtcsync.data.model.enums.UserRole

@Composable
fun Navigation(navController: NavHostController,
    notificationTarget: br.com.fiap.wtcsync.util.ChatNotificationTarget? = null,
    onNotificationConsumed: () -> Unit = {}) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(application)
    )
    val role by authViewModel.currentRole.collectAsState()

    val currentEntry by navController.currentBackStackEntryAsState()
    val route = currentEntry?.destination?.route
    val app = application as WtcSyncApp
    LaunchedEffect(notificationTarget, role, route) {
        val target = notificationTarget ?: return@LaunchedEffect
        if (role == null || route in listOf(null, "entry", "login", "register")) return@LaunchedEffect
        if (!target.matches(app.sessionManager.currentEmail)) {
            onNotificationConsumed()
        } else if (role == UserRole.OPERATOR || role == UserRole.ATENDENTE) {
            navController.navigate("message_screen/${Uri.encode(target.customerId)}/${Uri.encode("Atendimento")}") {
                popUpTo("home")
                launchSingleTop = true
            }
            onNotificationConsumed()
        } else if (route != "home") {
            navController.navigate("home") { popUpTo("home"); launchSingleTop = true }
        }
    }

    val reduceMotion = LocalReduceMotion.current
    NavHost(
        navController = navController, startDestination = "entry",
        enterTransition = {
            if (reduceMotion) fadeIn(tween(WtcMotion.SHORT))
            else fadeIn(tween(WtcMotion.MEDIUM)) + slideInHorizontally(tween(WtcMotion.LONG)) { it / 10 }
        },
        exitTransition = { fadeOut(tween(WtcMotion.SHORT)) },
        popEnterTransition = {
            if (reduceMotion) fadeIn(tween(WtcMotion.SHORT))
            else fadeIn(tween(WtcMotion.MEDIUM)) + slideInHorizontally(tween(WtcMotion.LONG)) { -it / 20 }
        },
        popExitTransition = {
            if (reduceMotion) fadeOut(tween(WtcMotion.SHORT))
            else fadeOut(tween(WtcMotion.SHORT)) + slideOutHorizontally(tween(WtcMotion.MEDIUM)) { it / 10 }
        }
    ) {
        composable("entry") {
            EntryScreen(
                viewModel = authViewModel,
                onLoginClick = { authViewModel.useManualLogin(); navController.navigate("login") },
                onRegisterClick = { authViewModel.useManualLogin(); navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("entry") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
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
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                userRole = role,
                openChatRequest = notificationTarget?.takeIf { it.matches(app.sessionManager.currentEmail) }?.requestId,
                onChatOpened = onNotificationConsumed,
                onLogout = {
                    app.pushRegistration.revoke(app.sessionManager.currentToken)
                    authViewModel.logout()
                    navController.navigate("entry") { popUpTo("home") { inclusive = true } }
                }
            )
        }
        composable(
            route = "message_screen/{clienteId}/{contactName}",
            arguments = listOf(
                navArgument("clienteId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            if (role != UserRole.OPERATOR && role != UserRole.ATENDENTE) { AccessDeniedScreen(navController); return@composable }
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            val contactName = backStackEntry.arguments?.getString("contactName") ?: "Contato"
            MessageScreen(
                clienteId = clienteId,
                contactName = contactName,
                navController = navController
            )
        }
        composable("campaigns") {
            if (role != UserRole.OPERATOR) { AccessDeniedScreen(navController); return@composable }
            val listViewModel: CampaignListViewModel = viewModel(
                factory = CampaignListViewModelFactory(application)
            )
            CampaignListScreen(
                viewModel = listViewModel,
                onCampaignClick = { id -> navController.navigate("campaigns/$id") },
                onCreateClick = { navController.navigate("campaigns/create") },
                userRole = role
            )
        }
        composable(
            route = "campaigns/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            if (role != UserRole.OPERATOR) { AccessDeniedScreen(navController); return@composable }
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val detailViewModel: CampaignDetailViewModel = viewModel(
                factory = CampaignDetailViewModelFactory(application, id)
            )
            CampaignDetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                userRole = role
            )
        }
        composable("campaigns/create") {
            if (role != UserRole.OPERATOR) { AccessDeniedScreen(navController); return@composable }
            val createViewModel: CreateCampaignViewModel = viewModel(
                factory = CreateCampaignViewModelFactory(application)
            )
            CreateCampaignScreen(
                viewModel = createViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate("campaigns") {
                        popUpTo("campaigns") { inclusive = true }
                    }
                }
            )
        }
        composable("clientes/novo") {
            if (role != UserRole.OPERATOR && role != UserRole.ATENDENTE) { AccessDeniedScreen(navController); return@composable }
            ClientFormScreen(
                clienteId = null,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable(
            route = "clientes/{id}/editar?fromChat={fromChat}",
            arguments = listOf(navArgument("id") { type = NavType.StringType },
                navArgument("fromChat") { type = NavType.BoolType; defaultValue = false })
        ) { backStack ->
            if (role != UserRole.OPERATOR && role != UserRole.ATENDENTE) { AccessDeniedScreen(navController); return@composable }
            ClientFormScreen(
                clienteId = backStack.arguments?.getString("id"),
                fromConversation = backStack.arguments?.getBoolean("fromChat") == true,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("segments/create") {
            if (role != UserRole.OPERATOR) { AccessDeniedScreen(navController); return@composable }
            val createViewModel: CreateSegmentViewModel = viewModel(
                factory = CreateSegmentViewModelFactory(application)
            )
            CreateSegmentScreen(
                viewModel = createViewModel,
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    navController: NavHostController,
    userRole: UserRole?,
    openChatRequest: String? = null,
    onChatOpened: () -> Unit = {},
    onLogout: () -> Unit
) {
    if (userRole == null) {
        Column { Text("Sessão indisponível"); WtcButton(onClick = onLogout) { Text("Entrar novamente") } }
        return
    }
    if (userRole == UserRole.CLIENTE) {
        val tabs = listOf(BottomNavTab.CHAT, BottomNavTab.ULTIMA_CAMPANHA, BottomNavTab.MEU_PERFIL, BottomNavTab.OUTRAS)
        var clientTab by rememberSaveable { mutableStateOf(BottomNavTab.CHAT) }
        LaunchedEffect(openChatRequest) {
            if (openChatRequest != null) { clientTab = BottomNavTab.CHAT; onChatOpened() }
        }
        val tabState = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
        Scaffold(
            bottomBar = { BottomNavBar(tabs, clientTab, { clientTab = it }) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                tabState.SaveableStateProvider(clientTab) {
                    when (clientTab) {
                        BottomNavTab.OUTRAS -> br.com.fiap.wtcsync.ui.client.ClientMoreScreen(onLogout)
                        BottomNavTab.MEU_PERFIL -> br.com.fiap.wtcsync.ui.client.ClientProfileScreen()
                        BottomNavTab.ULTIMA_CAMPANHA -> ClientCampaignScreen(
                            navController.context.applicationContext as Application)
                        else -> ClientSupportHome()
                    }
                }
            }
        }
        return
    }
    val availableTabs = if (userRole == UserRole.OPERATOR) {
        listOf(BottomNavTab.CLIENTES, BottomNavTab.SEGMENTOS, BottomNavTab.CAMPANHAS, BottomNavTab.CHAT)
    } else {
        listOf(BottomNavTab.CHAT, BottomNavTab.CLIENTES)
    }

    var selectedTab by rememberSaveable { mutableStateOf(BottomNavTab.CHAT) }

    if (selectedTab !in availableTabs) {
        selectedTab = BottomNavTab.CHAT
    }

    Scaffold(
        topBar = { AccountBar(if (userRole == UserRole.OPERATOR) "Operador" else "Atendente", onLogout) },
        bottomBar = {
            BottomNavBar(
                tabs = availableTabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                BottomNavTab.CLIENTES -> ClientesScreen(
                    onCreateClick = { navController.navigate("clientes/novo") },
                    onClienteClick = { id -> navController.navigate("clientes/$id/editar") }
                )
                BottomNavTab.SEGMENTOS -> SegmentosScreen(
                    application = navController.context.applicationContext as Application,
                    userRole = userRole,
                    onCreateClick = { navController.navigate("segments/create") }
                )
                BottomNavTab.CAMPANHAS -> {
                    when (userRole) {
                        UserRole.OPERATOR -> {
                            val listViewModel: CampaignListViewModel = viewModel(
                                factory = CampaignListViewModelFactory(
                                    navController.context.applicationContext as Application
                                )
                            )
                            val lifecycleOwner = LocalLifecycleOwner.current
                            var isFirstResume by rememberSaveable { mutableStateOf(true) }
                            DisposableEffect(lifecycleOwner) {
                                val observer = LifecycleEventObserver { _, event ->
                                    if (event == Lifecycle.Event.ON_RESUME) {
                                        if (!isFirstResume) {
                                            listViewModel.refresh()
                                        }
                                        isFirstResume = false
                                    }
                                }
                                lifecycleOwner.lifecycle.addObserver(observer)
                                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                            }
                            CampaignListScreen(
                                viewModel = listViewModel,
                                onCampaignClick = { id ->
                                    navController.navigate("campaigns/$id")
                                },
                                onCreateClick = {
                                    navController.navigate("campaigns/create")
                                },
                                userRole = userRole
                            )
                        }
                        UserRole.CLIENTE -> {
                            ClientCampaignScreen(
                                application = navController.context.applicationContext as Application
                            )
                        }
                        else -> {}
                    }
                }
                BottomNavTab.MEU_PERFIL, BottomNavTab.ULTIMA_CAMPANHA, BottomNavTab.OUTRAS -> Unit
                BottomNavTab.CHAT -> ChatListScreen(
                    onChatClick = { id, name ->
                        navController.navigate(
                            "message_screen/${Uri.encode(id)}/${Uri.encode(name)}"
                        )
                    },
                    onCreateClick = { navController.navigate("clientes/novo") }
                )
            }
        }
    }
}

/** Canal único do cliente. Busca o registro pelo e-mail autenticado,
 * nunca seleciona arbitrariamente a primeira conversa da lista. */
@Composable
private fun ClientSupportHome() {
    val app = LocalContext.current.applicationContext as WtcSyncApp
    var clientId by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableStateOf(0) }
    LaunchedEffect(app, retry) {
        loading = true
        error = null
        try {
            val email = app.sessionManager.currentEmail
            if (email.isNullOrBlank()) {
                error = "Sua sessão não está disponível. Entre novamente."
            } else {
                clientId = app.apiService.clientApi.getMyClient().id
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            error = when (e.code()) {
                401 -> "Sua sessão expirou. Saia e entre novamente."
                403 -> "O servidor recusou o acesso ao atendimento (403). Saia e entre novamente."
                404 -> "A rota de atendimento não está disponível no servidor (404)."
                in 500..599 -> "O servidor não conseguiu carregar seu atendimento (${e.code()}). Tente novamente."
                else -> "Não foi possível carregar o atendimento (${e.code()})."
            }
        } catch (_: java.io.IOException) {
            error = "Não foi possível conectar ao atendimento. Verifique sua conexão e tente novamente."
        } catch (_: Exception) {
            error = "O servidor retornou um atendimento inválido. Tente novamente."
        } finally {
            loading = false
        }
    }
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        clientId != null -> MessageScreen(
            clienteId = clientId!!,
            contactName = "Atendimento WTCSync",
            navController = androidx.navigation.compose.rememberNavController(),
            showBackButton = false
        )
        else -> WtcStatePanel("Atendimento indisponível", error, onRetry = { retry += 1 })
    }
}

@Composable
private fun AccountBar(label: String, onLogout: () -> Unit) {
    WtcAccountBar(label, onLogout)
}

@Composable
private fun AccessDeniedScreen(navController: NavHostController) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Seu perfil não tem acesso a esta tela.")
        WtcButton(onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }) {
            Text("Voltar ao início")
        }
    }
}
