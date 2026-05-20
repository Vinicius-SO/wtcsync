package br.com.fiap.wtcsync.campaigns.data

import br.com.fiap.wtcsync.campaigns.data.dto.ScheduleCampaignDto
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

    suspend fun scheduleCampaign(id: String, scheduledAt: String): Resource<Campaign> {
        return try {
            val response = campaignApi.scheduleCampaign(id, ScheduleCampaignDto(scheduledAt))
            Resource.Success(response.toDomain())
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                409 -> "Campanha não pode ser agendada no estado atual"
                404 -> "Campanha não encontrada"
                403 -> "Acesso negado. Apenas operadores podem agendar campanhas"
                else -> "Erro ao agendar campanha (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao agendar campanha")
        }
    }

    suspend fun deleteCampaign(id: String): Resource<Unit> {
        return try {
            campaignApi.deleteCampaign(id)
            Resource.Success(Unit)
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                409 -> "Exclusão não permitida: campanhas enviadas ou agendadas não podem ser excluídas"
                404 -> "Campanha não encontrada: ela pode ter sido removida por outro usuário"
                403 -> "Exclusão negada: apenas operadores têm permissão para excluir campanhas"
                else -> "Erro ao excluir campanha (código ${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao excluir campanha")
        }
    }

    suspend fun sendCampaign(id: String): Resource<Campaign> {
        return try {
            val response = campaignApi.sendCampaign(id)
            Resource.Success(response.toDomain())
        } catch (e: retrofit2.HttpException) {
            val msg = when (e.code()) {
                409 -> "Campanha não pode ser enviada no estado atual"
                404 -> "Campanha não encontrada"
                403 -> "Acesso negado. Apenas operadores podem enviar campanhas"
                else -> "Erro ao enviar campanha (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erro ao enviar campanha")
        }
    }
}
