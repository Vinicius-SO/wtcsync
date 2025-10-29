package br.com.fiap.wtcsync.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.fiap.wtcsync.data.model.enums.UserRole
import br.com.fiap.wtcsync.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory()),
    onRegisterSuccess: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val userState by viewModel.userState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.CLIENTE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Registrar") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Registrar Novo Usuário",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(expanded = false, onExpandedChange = {})
            {
                OutlinedTextField(
                    value = role.name,
                    onValueChange = {},
                    label = { Text("Role") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.register(email, password, name, role) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar")
            }

            if (userState is Resource.Loading) {
                CircularProgressIndicator()
            }

            LaunchedEffect(userState) {
                when (val state = userState) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess(state.data?.email ?: "")
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