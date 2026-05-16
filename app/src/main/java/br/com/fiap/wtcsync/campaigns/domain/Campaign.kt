package br.com.fiap.wtcsync.campaigns.domain

data class Campaign(
    val id: String,
    val title: String,
    val body: String,
    val segmentId: String,
    val status: String,
    val mediaUrl: String?,
    val deeplink: String?,
    val actions: List<CampaignAction>,
    val actionUrls: Map<String, String>,
    val stats: CampaignStats,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String
)

data class CampaignAction(
    val action: String,
    val title: String
)

data class CampaignStats(
    val totalTargeted: Int,
    val totalDelivered: Int,
    val totalRead: Int,
    val totalFailed: Int
)

data class CreateCampaignRequest(
    val title: String,
    val body: String,
    val segmentId: String,
    val mediaUrl: String?,
    val deeplink: String?,
    val actions: List<CampaignAction>,
    val actionUrls: Map<String, String>
)
