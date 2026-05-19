package br.com.fiap.wtcsync.campaigns.data.dto

import br.com.fiap.wtcsync.campaigns.domain.CreateCampaignRequest

data class CreateCampaignDto(
    val title: String,
    val body: String,
    val segmentId: String,
    val mediaUrl: String?,
    val deeplink: String?,
    val actions: List<CampaignActionDto>,
    val actionUrls: Map<String, String>
)

fun CreateCampaignRequest.toDto() = CreateCampaignDto(
    title = title,
    body = body,
    segmentId = segmentId,
    mediaUrl = mediaUrl,
    deeplink = deeplink,
    actions = actions.map { CampaignActionDto(it.action, it.title) },
    actionUrls = actionUrls
)
