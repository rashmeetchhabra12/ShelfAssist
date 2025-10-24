package com.plcoding.bookpedia.book.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationRequestDto(
    val titles: List<String>
)

@Serializable
data class RecommendationResponseDto(
    val recommendations: List<String>,
    val found_titles: List<String>? = null,
    val not_found_titles: List<String>? = null
)