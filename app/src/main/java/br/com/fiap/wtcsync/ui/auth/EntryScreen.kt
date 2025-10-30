package br.com.fiap.wtcsync.ui.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

val YellowPrimary = Color(0xFFFFCC00)
val BlackPrimary = Color(0xFF1C1C1E)
val LightBackground = Color(0xFFFFF9EF)
val DividerColor = Color(0xFFE0E0E0)
val SubtitleGray = Color(0xFF8A8A8E) // <-- COR ADICIONADA

// --- DEFINIÇÃO DAS FONTES ---
// (Certifique-se de ter os arquivos .ttf em res/font)

// Ex: res/font/roboto_bold.ttf
val robotoFamily = FontFamily(
    Font(R.font.roboto, FontWeight.Bold)
)

// Ex: res/font/nunito_regular.ttf
val nunitoFamily = FontFamily(
    Font(R.font.nunito, FontWeight.Normal)
)
// --- FIM DAS FONTES ---


@Composable
fun EntryScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())
    val userState by viewModel.userState.collectAsState()
    var successfulUser by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken!!
                viewModel.loginWithGoogle(idToken)
            } catch (e: ApiException) {
                errorMessage = "Google Sign-in failed: ${e.message}"
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 80.dp) // Espaçamento que estava no subtítulo
            ) {
                // Ícone
                Image(
                    painter = painterResource(id = R.drawable.wtcsync_logo),
                    contentDescription = "Logo WTC Sync",
                    modifier = Modifier.size(120.dp) // Tamanho ajustado (300.dp era muito grande)
                )

                Spacer(Modifier.width(24.dp))

                // Textos
                Column {
                    // Título
                    Text(
                        text = "WTC Sync",
                        fontFamily = robotoFamily, // <-- Fonte Roboto
                        fontWeight = FontWeight.Black,
                        fontSize = 40.sp, // Tamanho grande
                        color = BlackPrimary // Cor principal
                    )
                    // Subtítulo
                    Text(
                        text = "Em perfeita sintonia com cada cliente.",
                        fontFamily = nunitoFamily, // <-- Fonte Nunito
                        fontSize = 20.sp, // Tamanho menor
                        color = SubtitleGray // <-- Cor cinza da imagem
                    )
                }
            }


            // Botão Login
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Login", color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botão Cadastrar
            Button(
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(containerColor = BlackPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Cadastrar", color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divisor com "ou"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(color = DividerColor, thickness = 1.dp, modifier = Modifier.weight(1f))
                Text(
                    "ou",
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Divider(color = DividerColor, thickness = 1.dp, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Entrar com Google
            OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val client = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(client.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Entrar com Google", color = Color.Black, fontWeight = FontWeight.Normal, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Entrar com Apple
//            OutlinedButton(
//                onClick = { /* TODO: Apple Sign In */ },
//                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Black),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_apple_logo),
//                    contentDescription = "Apple logo",
//                    tint = Color.White,
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(Modifier.width(8.dp))
//                Text("Entrar com Apple", color = Color.White)
//            }
        }
    }

    LaunchedEffect(userState) {
        when (val state = userState) {
            is Resource.Success -> successfulUser = state.data
            is Resource.Error -> errorMessage = state.message
            else -> {}
        }
    }

    successfulUser?.let {
        AlertDialog(
            onDismissRequest = { successfulUser = null },
            title = { Text("Login Bem-sucedido!") },
            text = { Text("Bem-vindo ${it.email}") },
            confirmButton = {
                Button(onClick = {
                    onLoginSuccess(it.email ?: "")
                    successfulUser = null
                }) { Text("OK") }
            }
        )
    }

    errorMessage?.let {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Erro de Login") },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}