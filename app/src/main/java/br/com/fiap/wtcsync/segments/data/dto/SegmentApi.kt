package br.com.fiap.wtcsync.segments.data.dto

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SegmentApi {
    @GET("segments")
    suspend fun getSegments(): List<SegmentDto>

    @POST("segments")
    suspend fun createSegment(@Body dto: CreateSegmentDto): SegmentDto
}