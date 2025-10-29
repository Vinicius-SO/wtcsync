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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.R
import br.com.fiap.wtcsync.data.model.User
import br.com.fiap.wtcsync.util.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

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
                Toast.makeText(context, "Google Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.firts_logo),
            contentDescription = "Logo WTC Sync",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = "Bem-vindo ao WTC Sync",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login com E-mail")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(client.signInIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar com Google")
        }
    }

    LaunchedEffect(userState) {
        when (val state = userState) {
            is Resource.Success -> {
                successfulUser = state.data
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    if (successfulUser != null) {
        AlertDialog(
            onDismissRequest = { successfulUser = null },
            title = { Text("Login Bem-sucedido!") },
            text = { Text("Bem-vindo ${successfulUser?.email}") },
            confirmButton = {
                Button(
                    onClick = {
                        onLoginSuccess(successfulUser?.email ?: "")
                        successfulUser = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}