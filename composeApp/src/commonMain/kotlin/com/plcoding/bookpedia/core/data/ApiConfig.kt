package com.plcoding.bookpedia.core.data

/**
 * API Configuration
 * Update BASE_URL with your actual Render deployment URL
 */
object ApiConfig {
    // TODO: Replace with your actual Render URL
    const val RECOMMENDATION_API_BASE_URL = "https://book-recommendation-api.onrender.com"

    // For testing locally (when running API on your machine)
    // const val RECOMMENDATION_API_BASE_URL = "http://10.0.2.2:5000" // Android emulator
    // const val RECOMMENDATION_API_BASE_URL = "http://localhost:5000" // iOS simulator
}