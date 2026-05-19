package br.com.fiap.wtcsync.campaigns.data

import br.com.fiap.wtcsync.campaigns.data.dto.toDomain
import br.com.fiap.wtcsync.campaigns.data.dto.toDto
import br.com.fiap.wtcsync.campaigns.domain.Campaign
import br.com.fiap.wtcsync.campaigns.domain.CreateCampaignRequest
import br.com.fiap.wtcsync.util.Resource

class CampaignRepository(private val campaignApi: CampaignApi) {

    suspend fun listCampaigns(): Resource<List<Campaign>> {
        return try {
            val response = campaignApi.listCampaigns()
            Resource.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao carregar campanhas")
        }
    }

    suspend fun getCampaign(id: String): Resource<Campaign> {
        return try {
            val response = campaignApi.getCampaign(id)
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Campanha não encontrada")
        }
    }

    suspend fun createCampaign(request: CreateCampaignRequest): Resource<Campaign> {
        return try {
            val response = campaignApi.createCampaign(request.toDto())
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao criar campanha")
        }
    }
}
