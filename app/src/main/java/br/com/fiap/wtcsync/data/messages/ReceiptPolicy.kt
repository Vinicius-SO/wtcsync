package br.com.fiap.wtcsync.data.messages

fun receiptLabel(status: String): String = when (status) {
    "ENTREGUE" -> "Entregue"
    "LIDO" -> "Lido"
    "FALHA" -> "Falha"
    else -> "Enviado"
}
