package br.com.fiap.wtcsync.data.model

data class Campanha(
    val id: String,
    val title: String,
    val status: String,
    val statsSent: Int? = null,
    val statsRead: Int? = null,
    val scheduleInfo: String? = null,
    val eventDate: String = "",
    val eventLocation: String = "",
    val eventDescription: String = "",
    val receivedDate: String = ""
)

val mockCampanhas = listOf(
    Campanha(
        id = "1",
        title = "Financial Shift 2025",
        status = "ENVIADA",
        statsSent = 118,
        statsRead = 74,
        eventDate = "15 de Junho de 2025",
        eventLocation = "WTC São Paulo, SP",
        eventDescription = "Prepare-se para a maior imersão em tendências do mercado financeiro da América Latina. O Financial Shift 2025 reunirá os principais arquitetos da economia digital para discutir estratégias de escala, conformidade e novas tecnologias de crédito. Uma oportunidade única de networking e insights estratégicos para o seu negócio.",
        receivedDate = "Recebido em 12/05/2024 às 14:30"
    ),
    Campanha(
        id = "2",
        title = "Semana do Cliente WTC",
        status = "AGENDADA",
        scheduleInfo = "Disparo em 5 dias",
        eventDate = "20 de Junho de 2025",
        eventLocation = "WTC São Paulo, SP",
        eventDescription = "A Semana do Cliente WTC é um evento exclusivo que oferece condições especiais e benefícios únicos para nossos clientes. Durante uma semana inteira, você terá acesso a ofertas personalizadas, conteúdo relevante e muito mais.",
        receivedDate = "Recebido em 10/05/2024 às 09:00"
    ),
    Campanha(
        id = "3",
        title = "Promoção Inverno 2025",
        status = "RASCUNHO",
        eventDate = "01 de Julho de 2025",
        eventLocation = "Online",
        eventDescription = "Aqueça seus negócios com a Promoção Inverno 2025. Condições especiais para aquecer as vendas durante a estação mais fria do ano. Não perca essa oportunidade.",
        receivedDate = "Criado em 08/05/2024 às 16:45"
    )
)
