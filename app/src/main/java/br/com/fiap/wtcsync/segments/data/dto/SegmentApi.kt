package br.com.fiap.wtcsync.segments.data.dto

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SegmentApi {
    @GET("api/segments")
    suspend fun getSegments(): List<SegmentDto>

    @POST("api/segments")
    suspend fun createSegment(@Body dto: CreateSegmentDto): SegmentDto
}