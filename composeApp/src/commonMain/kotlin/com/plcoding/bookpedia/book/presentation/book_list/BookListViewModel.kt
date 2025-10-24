@file:OptIn(FlowPreview::class)

package com.plcoding.bookpedia.book.presentation.book_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.bookpedia.book.data.repository.RecommendationRepository
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.book.domain.BookRepository
import com.plcoding.bookpedia.core.domain.onError
import com.plcoding.bookpedia.core.domain.onSuccess
import com.plcoding.bookpedia.core.presentation.toUiText
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookListViewModel(
    private val bookRepository: BookRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    private var cachedBooks = emptyList<Book>()
    private var searchJob: Job? = null
    private var observeFavoriteJob: Job? = null
    private var recommendationsJob: Job? = null
    private var lastFavoriteCount = 0 // Track favorite count changes

    private val _state = MutableStateFlow(BookListState())
    val state = _state
        .onStart {
            if(cachedBooks.isEmpty()) {
                observeSearchQuery()
            }
            observeFavoriteBooks()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            _state.value
        )

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnBookClick -> {
                // Handle book click if needed
            }
            is BookListAction.OnSearchQueryChange -> {
                _state.update {
                    it.copy(searchQuery = action.query)
                }
            }
            is BookListAction.OnTabSelected -> {
                _state.update {
                    it.copy(selectedTabIndex = action.index)
                }
                // Load recommendations when user switches to recommendations tab
                if (action.index == 2) {
                    if (_state.value.recommendedBooks.isEmpty()) {
                        loadRecommendations()
                    }
                }
            }
            is BookListAction.OnLoadRecommendations -> {
                loadRecommendations()
            }
        }
    }

    private fun observeFavoriteBooks() {
        observeFavoriteJob?.cancel()
        observeFavoriteJob = bookRepository
            .getFavoriteBooks()
            .onEach { favoriteBooks ->
                val currentCount = favoriteBooks.size
                val previousCount = lastFavoriteCount

                _state.update {
                    it.copy(favoriteBooks = favoriteBooks)
                }

                // If favorites changed and we have loaded recommendations before
                // AND we're on the recommendations tab, refresh them
                if (previousCount != currentCount && _state.value.recommendedBooks.isNotEmpty()) {
                    // Clear current recommendations
                    _state.update {
                        it.copy(recommendedBooks = emptyList())
                    }

                    // Reload if on recommendations tab
                    if (_state.value.selectedTabIndex == 2 && currentCount > 0) {
                        loadRecommendations()
                    }
                }

                lastFavoriteCount = currentCount
            }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        state
            .map { it.searchQuery }
            .distinctUntilChanged()
            .debounce(500L)
            .onEach { query ->
                when {
                    query.isBlank() -> {
                        _state.update {
                            it.copy(
                                errorMessage = null,
                                searchResults = cachedBooks
                            )
                        }
                    }
                    query.length >= 2 -> {
                        searchJob?.cancel()
                        searchJob = searchBooks(query)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun searchBooks(query: String) = viewModelScope.launch {
        _state.update {
            it.copy(isLoading = true)
        }

        bookRepository
            .searchBooks(query)
            .onSuccess { searchResults ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        searchResults = searchResults
                    )
                }
            }
            .onError { error ->
                _state.update {
                    it.copy(
                        searchResults = emptyList(),
                        isLoading = false,
                        errorMessage = error.toUiText()
                    )
                }
            }
    }

    private fun loadRecommendations() {
        // Check if there are favorites first
        if (_state.value.favoriteBooks.isEmpty()) {
            _state.update {
                it.copy(
                    isLoadingRecommendations = false,
                    recommendedBooks = emptyList(),
                    recommendationError = null
                )
            }
            return
        }

        recommendationsJob?.cancel()
        recommendationsJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingRecommendations = true,
                    recommendationError = null
                )
            }

            recommendationRepository
                .getRecommendations()
                .onSuccess { recommendations ->
                    _state.update {
                        it.copy(
                            isLoadingRecommendations = false,
                            recommendedBooks = recommendations,
                            recommendationError = null
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(
                            isLoadingRecommendations = false,
                            recommendedBooks = emptyList(),
                            recommendationError = error.toUiText()
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        observeFavoriteJob?.cancel()
        recommendationsJob?.cancel()
    }
}