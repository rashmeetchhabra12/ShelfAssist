package com.plcoding.bookpedia.book.data.repository

import com.plcoding.bookpedia.book.data.database.FavoriteBookDao
import com.plcoding.bookpedia.book.data.mappers.toBook
import com.plcoding.bookpedia.book.data.network.RecommendationRemoteDataSource
import com.plcoding.bookpedia.book.data.network.RemoteBookDataSource
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.core.domain.DataError
import com.plcoding.bookpedia.core.domain.Result
import kotlinx.coroutines.flow.first

class RecommendationRepository(
    private val recommendationDataSource: RecommendationRemoteDataSource,
    private val remoteBookDataSource: RemoteBookDataSource,
    private val favoriteBookDao: FavoriteBookDao
) {
    /**
     * Get book recommendations based on user's favorite books
     */
    suspend fun getRecommendations(): Result<List<Book>, DataError.Remote> {
        return try {
            // Get user's favorite book titles from database
            val favoriteEntities = favoriteBookDao.getFavoriteBooks().first()
            val favoriteTitles = favoriteEntities.map { it.title }

            // Need at least one favorite book to get recommendations
            if (favoriteTitles.isEmpty()) {
                return Result.Success(emptyList())
            }

            // Call recommendation API
            when (val result = recommendationDataSource.getRecommendations(favoriteTitles)) {
                is Result.Success -> {
                    // Search for each recommended book to get full details
                    val recommendedBooks = mutableListOf<Book>()

                    result.data.recommendations.take(10).forEach { title ->
                        // Search for the book by title
                        when (val searchResult = remoteBookDataSource.searchBooks(title, null)) {
                            is Result.Success -> {
                                // Add the first match (most relevant)
                                searchResult.data.results.firstOrNull()?.let { dto ->
                                    recommendedBooks.add(dto.toBook())
                                }
                            }
                            is Result.Error -> {
                                // Skip this book if search fails
                            }
                        }
                    }

                    Result.Success(recommendedBooks)
                }
                is Result.Error -> Result.Error(result.error)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(DataError.Remote.UNKNOWN)
        }
    }

    /**
     * Check if recommendation service is available
     */
    suspend fun checkServiceHealth(): Result<Unit, DataError.Remote> {
        return when (val result = recommendationDataSource.checkHealth()) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.error)
        }
    }
}