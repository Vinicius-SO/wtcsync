package br.com.fiap.wtcsync.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// --- Modelos de Dados Estáticos ---

enum class Sender {
    ME, OTHER
}

data class Message(
    val id: Int,
    val text: String,
    val sender: Sender
)

val dummyMessagesAlice = listOf(
    Message(1, "Oi Alice!", Sender.ME),
    Message(2, "Olá! Como você está?", Sender.OTHER)
)

val dummyMessagesBob = listOf(
    Message(1, "E aí Bob, tudo certo pra amanhã?", Sender.ME),
    Message(2, "Tudo sim.", Sender.OTHER),
    Message(3, "Vamos conversar em breve!", Sender.OTHER)
)

val dummyMessagesCharlie = listOf(
    Message(1, "Charlie, podemos falar amanhã?", Sender.ME),
    Message(2, "Claro.", Sender.OTHER),
    Message(3, "Você ainda ta disponível amanhã?", Sender.OTHER)
)

val dummyMessagesDavid = listOf(
    Message(1, "Como foi o fim de semana?", Sender.ME),
    Message(2, "Foi ótimo, descansei bastante.", Sender.OTHER),
    Message(3, "Animado pra semana!", Sender.OTHER)
)

val dummyMessagesEva = listOf(
    Message(1, "Oi Eva, alguma pendência?", Sender.ME),
    Message(2, "Oi! Sim.", Sender.OTHER),
    Message(3, "Preciso da sua avaliação no projeto.", Sender.OTHER)
)

val dummyMessagesFrank = listOf(
    Message(1, "Conseguiu subir os documentos?", Sender.ME),
    Message(2, "Consegui sim.", Sender.OTHER),
    Message(3, "Te enviei os arquivos.", Sender.OTHER)
)

val dummyMessagesGrace = listOf(
    Message(1, "Grace, confirmado para as 14h?", Sender.ME),
    Message(2, "Oi! Tive um imprevisto.", Sender.OTHER),
    Message(3, "Podemos remarcar?", Sender.OTHER)
)

// 3. Lista padrão
val dummyMessagesDefault = listOf(
    Message(1, "Olá! Esta é uma conversa padrão.", Sender.OTHER),
)


val OnlineGreen = Color(0xFF4CAF50)


// Tela principal de chat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    contactName: String,
    navController: NavController
) {
    var messageText by remember { mutableStateOf("") }

    // Lista de mensagens
    val messagesToShow = when (contactName) {
        "Alice" -> dummyMessagesAlice
        "Bob" -> dummyMessagesBob
        "Charlie" -> dummyMessagesCharlie
        "David" -> dummyMessagesDavid
        "Eva" -> dummyMessagesEva
        "Frank" -> dummyMessagesFrank
        "Grace" -> dummyMessagesGrace
        else -> dummyMessagesDefault // Para qualquer outro
    }

    Scaffold(
        topBar = {
            MessageTopBar(
                contactName = contactName,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            MessageInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSendClick = {
                    messageText = ""
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(messagesToShow.reversed()) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTopBar(
    contactName: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = contactName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OnlineGreen,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = "Online",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar"
                )
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Menu */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Mais opções"
                )
            }
        },
    )
}

@Composable
fun MessageBubble(message: Message) {
    val isMe = message.sender == Sender.ME
    val bubbleColor = if (isMe) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 0.dp,
        bottomEnd = if (isMe) 0.dp else 16.dp
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            tonalElevation = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO: Anexar */ }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Anexar",
                    modifier = Modifier.size(28.dp)
                )
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Mensagem...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                maxLines = 5
            )
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


// --- Preview ---

@Preview(showBackground = true)
@Composable
fun MessageScreenPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        MessageScreen(
            contactName = "Alice",
            navController = navController
        )
    }
}