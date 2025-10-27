package br.com.fiap.wtcsync.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import br.com.fiap.wtcsync.data.model.Cliente

class ChatListViewModel : ViewModel() {


    private val _clientes = mutableStateListOf(
        Cliente("Alice", "Olá! Como você está?", "10 mins ago"),
        Cliente("Bob", "Vamos conversar em breve!", "32 mins ago"),
        Cliente("Charlie", "Você ainda ta disponível amanhã?", "1 hr ago"),
        Cliente("David", "Animado pra semana!", "2 hrs ago"),
        Cliente("Eva", "Preciso da sua avaliação no projeto.", "3 hrs ago"),
        Cliente("Frank", "Te enviei os arquivos.", "5 hrs ago"),
        Cliente("Grace", "Podemos remarcar?", "7 hrs ago")
    )

    // Texto digitado no campo de busca
    var searchQuery by mutableStateOf("")
        private set

    // Retorna lista filtrada conforme busca
    val clientesFiltrados: List<Cliente>
        get() = if (searchQuery.isEmpty()) _clientes
        else _clientes.filter { it.nome.contains(searchQuery, ignoreCase = true) }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }
}


