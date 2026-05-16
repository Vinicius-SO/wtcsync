package br.com.fiap.wtcsync.campaigns.data.dto

import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.campaigns.domain.CampaignAction
import br.com.fiap.wtcsync.campaigns.domain.CampaignStats
import com.google.gson.annotations.SerializedName

data class CampaignDto(
    val id: String,
    val title: String,
    val body: String,
    val segmentId: String,
    val status: String,
    val mediaUrl: String?,
    val deeplink: String?,
    val actions: List<CampaignActionDto>,
    val actionUrls: Map<String, String>,
    val stats: CampaignStatsDto,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String
)

data class CampaignActionDto(
    val action: String,
    val title: String
)

data class CampaignStatsDto(
    val totalTargeted: Int,
    val totalDelivered: Int,
    val totalRead: Int,
    val totalFailed: Int
)

fun CampaignDto.toDomain() = Campaign(
    id = id,
    title = title,
    body = body,
    segmentId = segmentId,
    status = status,
    mediaUrl = mediaUrl,
    deeplink = deeplink,
    actions = actions.map { it.toDomain() },
    actionUrls = actionUrls,
    stats = stats.toDomain(),
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CampaignActionDto.toDomain() = CampaignAction(
    action = action,
    title = title
)

fun CampaignStatsDto.toDomain() = CampaignStats(
    totalTargeted = totalTargeted,
    totalDelivered = totalDelivered,
    totalRead = totalRead,
    totalFailed = totalFailed
)
