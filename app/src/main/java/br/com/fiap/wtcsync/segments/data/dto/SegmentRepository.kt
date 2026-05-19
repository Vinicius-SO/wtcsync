package br.com.fiap.wtcsync.segments.data.dto

class SegmentRepository(private val api: SegmentApi) {
    suspend fun getSegments() = api.getSegments()
    suspend fun createSegment(dto: CreateSegmentDto) = api.createSegment(dto)
}