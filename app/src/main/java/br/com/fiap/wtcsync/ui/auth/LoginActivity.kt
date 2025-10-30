package br.com.fiap.wtcsync.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.util.NetworkUtils
import br.com.fiap.wtcsync.util.Resource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory()),
    onLoginSuccess: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val userState by viewModel.userState.collectAsState()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    // 1. Trocado Scaffold por Surface
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground // Cor de fundo da imagem
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Padding geral da tela
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // 3. Ícone de Voltar (sem TopAppBar)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(bottom = 16.dp) // Espaço abaixo do ícone
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }

            // 4. Logo
            Image(
                // Certifique-se que este é o drawable correto apenas para o ícone
                painter = painterResource(id = R.drawable.wtcsync_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(60.dp) // Tamanho ajustado para o ícone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Título "Login"
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = robotoFamily,
                fontWeight = FontWeight.Bold,
                color = BlackPrimary
            )

            Spacer(modifier = Modifier.height(32.dp)) // Espaço maior antes dos campos

            // 6. Campo de Email
            Text(
                text = "Email",
                style = MaterialTheme.typography.labelLarge, // Estilo para o label
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("JoeDoe@WTC.com", color = Color.Gray) }, // Placeholder
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
                // Label removido de dentro
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Campo de Senha
            Text(
                text = "Senha",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                placeholder = { Text("**************", color = Color.Gray) }, // Placeholder
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), // Para ocultar a senha
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
                // Label removido de dentro
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Link "Esqueceu a senha"
            TextButton(
                onClick = { /* TODO: Ação de esquecer a senha */ },
                modifier = Modifier.align(Alignment.Start),
                contentPadding = PaddingValues(0.dp) // Remove padding extra
            ) {
                Text(
                    "Esqueceu a senha ? Clique aqui",
                    color = SubtitleGray, // Cor cinza sutil
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Espaçador para empurrar o botão para baixo (opcional, ajuste conforme necessário)
            // Se quiser o botão colado, remova este Spacer
            // Se quiser o botão no fim da tela, use Spacer(Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))


            // 9. Botão Conectar
            Button(
                onClick = {
                    if (NetworkUtils.isConnected(context)) {
                        viewModel.login(email, senha)
                    } else {
                        Toast.makeText(context, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = YellowPrimary), // Cor Amarela
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    "Conectar",
                    color = Color.Black, // Texto Preto
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Lógica de Loading e Erro (mantida)
            if (userState is Resource.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            LaunchedEffect(userState) {
                when (val state = userState) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Bem-vindo ${state.data?.email}", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(state.data?.email ?: "")
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
}