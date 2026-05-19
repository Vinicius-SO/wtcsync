package br.com.fiap.wtcsync.campaigns.data

import br.com.fiap.wtcsync.campaigns.data.dto.CampaignDto
import br.com.fiap.wtcsync.campaigns.data.dto.CreateCampaignDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CampaignApi {

    @GET("api/campaigns")
    suspend fun listCampaigns(): List<CampaignDto>

    @GET("api/campaigns/{id}")
    suspend fun getCampaign(@Path("id") id: String): CampaignDto

    @POST("api/campaigns")
    suspend fun createCampaign(@Body request: CreateCampaignDto): CampaignDto
}
