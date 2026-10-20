package br.com.fiap.wtcsync.campaigns.data.dto

import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.campaigns.domain.CampaignStats

data class ReceivedCampaignDto(
    val id: String,
    val title: String,
    val body: String,
    val mediaUrl: String?,
    val actions: List<CampaignActionDto>?,
    val actionUrls: Map<String, String>?,
    val receivedAt: String
)

fun ReceivedCampaignDto.toDomain() = Campaign(
    id = id, title = title, body = body, mediaUrl = mediaUrl,
    actions = actions.orEmpty().map { it.toDomain() }, actionUrls = actionUrls.orEmpty(),
    segmentId = "", status = "sent", deeplink = null, stats = CampaignStats(0, 0, 0, 0),
    createdBy = "", createdAt = receivedAt, updatedAt = receivedAt
)
