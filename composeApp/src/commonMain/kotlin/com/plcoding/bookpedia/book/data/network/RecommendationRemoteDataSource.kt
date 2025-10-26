package com.plcoding.bookpedia.book.data.network

import com.plcoding.bookpedia.book.data.dto.RecommendationRequestDto
import com.plcoding.bookpedia.book.data.dto.RecommendationResponseDto
import com.plcoding.bookpedia.core.data.ApiConfig
import com.plcoding.bookpedia.core.domain.DataError
import com.plcoding.bookpedia.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface RecommendationRemoteDataSource {
    suspend fun getRecommendations(favoriteBookTitles: List<String>): Result<RecommendationResponseDto, DataError.Remote>
    suspend fun checkHealth(): Result<Boolean, DataError.Remote>
}

class KtorRecommendationRemoteDataSource(
    private val httpClient: HttpClient
) : RecommendationRemoteDataSource {

    companion object {
        private val BASE_URL = ApiConfig.RECOMMENDATION_API_BASE_URL
    }

    override suspend fun getRecommendations(
        favoriteBookTitles: List<String>
    ): Result<RecommendationResponseDto, DataError.Remote> {
        return try {
            val response = httpClient.post("$BASE_URL/recommend") {
                contentType(ContentType.Application.Json)
                setBody(RecommendationRequestDto(titles = favoriteBookTitles))
            }.body<RecommendationResponseDto>()

            Result.Success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Remote.UNKNOWN)
        }
    }

    override suspend fun checkHealth(): Result<Boolean, DataError.Remote> {
        return try {
            httpClient.get("$BASE_URL/health")
            Result.Success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Remote.UNKNOWN)
        }
    }
}