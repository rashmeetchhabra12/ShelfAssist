package com.plcoding.bookpedia.book.presentation.book_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp_bookpedia.composeapp.generated.resources.Res
import cmp_bookpedia.composeapp.generated.resources.favorites
import cmp_bookpedia.composeapp.generated.resources.no_favorite_books
import cmp_bookpedia.composeapp.generated.resources.no_search_results
import cmp_bookpedia.composeapp.generated.resources.search_results
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.book.presentation.book_list.components.BookGridItem
import com.plcoding.bookpedia.book.presentation.book_list.components.BookSearchBar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// Elegant Color Palette
private val BackgroundColor = Color(0xFFF5F3FF) // Soft lavender
private val CardBackground = Color.White
private val AccentCoral = Color(0xFFFF7F7F)
private val AccentMint = Color(0xFF7BDCB5)
private val TextPrimary = Color(0xFF2D3748)
private val TextSecondary = Color(0xFF718096)

@Composable
fun BookListScreenRoot(
    viewModel: BookListViewModel = koinViewModel(),
    onBookClick: (Book) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BookListScreen(
        state = state,
        onAction = { action ->
            when(action) {
                is BookListAction.OnBookClick -> onBookClick(action.book)
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
fun BookListScreen(
    state: BookListState,
    onAction: (BookListAction) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val pagerState = rememberPagerState { 3 }
    val searchResultsGridState = rememberLazyGridState()
    val favoriteBooksGridState = rememberLazyGridState()
    val recommendedBooksGridState = rememberLazyGridState()

    LaunchedEffect(state.searchResults) {
        searchResultsGridState.animateScrollToItem(0)
    }

    LaunchedEffect(state.selectedTabIndex) {
        pagerState.animateScrollToPage(state.selectedTabIndex)
    }

    LaunchedEffect(pagerState.currentPage) {
        onAction(BookListAction.OnTabSelected(pagerState.currentPage))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Discover Books",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Explore your next great read",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(20.dp))

            BookSearchBar(
                searchQuery = state.searchQuery,
                onSearchQueryChange = {
                    onAction(BookListAction.OnSearchQueryChange(it))
                },
                onImeSearch = {
                    keyboardController?.hide()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tabs
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            TabRow(
                selectedTabIndex = state.selectedTabIndex,
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        color = AccentCoral,
                        height = 3.dp,
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[state.selectedTabIndex])
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    )
                },
                divider = {}
            ) {
                ElegantTab(
                    selected = state.selectedTabIndex == 0,
                    onClick = { onAction(BookListAction.OnTabSelected(0)) },
                    text = stringResource(Res.string.search_results),
                    emoji = "🔍"
                )
                ElegantTab(
                    selected = state.selectedTabIndex == 1,
                    onClick = { onAction(BookListAction.OnTabSelected(1)) },
                    text = stringResource(Res.string.favorites),
                    emoji = "❤️"
                )
                ElegantTab(
                    selected = state.selectedTabIndex == 2,
                    onClick = { onAction(BookListAction.OnTabSelected(2)) },
                    text = "For You",
                    emoji = "✨"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { pageIndex ->
            when(pageIndex) {
                0 -> SearchResultsContent(
                    state = state,
                    onAction = onAction,
                    gridState = searchResultsGridState
                )
                1 -> FavoritesContent(
                    state = state,
                    onAction = onAction,
                    gridState = favoriteBooksGridState
                )
                2 -> RecommendationsContent(
                    state = state,
                    onAction = onAction,
                    gridState = recommendedBooksGridState
                )
            }
        }
    }
}

@Composable
private fun ElegantTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    emoji: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) AccentCoral else TextSecondary
            )
        }
    }
}

@Composable
private fun SearchResultsContent(
    state: BookListState,
    onAction: (BookListAction) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingState("Searching for books...")
        }

        AnimatedVisibility(
            visible = !state.isLoading,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            when {
                state.errorMessage != null -> {
                    EmptyState(
                        emoji = "😔",
                        title = "Oops!",
                        message = state.errorMessage.asString(),
                        accentColor = AccentCoral
                    )
                }
                state.searchResults.isEmpty() -> {
                    EmptyState(
                        emoji = "📚",
                        title = "No Results Found",
                        message = "Try searching with different keywords",
                        accentColor = AccentMint
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        itemsIndexed(
                            items = state.searchResults,
                            key = { index, book -> "${book.id}_$index" }
                        ) { _, book ->
                            BookGridItem(
                                book = book,
                                onClick = { onAction(BookListAction.OnBookClick(book)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    state: BookListState,
    onAction: (BookListAction) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    if(state.favoriteBooks.isEmpty()) {
        EmptyState(
            emoji = "❤️",
            title = "No Favorites Yet",
            message = "Start building your collection by adding books to favorites",
            accentColor = AccentCoral
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            itemsIndexed(
                items = state.favoriteBooks,
                key = { index, book -> "${book.id}_fav_$index" }
            ) { _, book ->
                BookGridItem(
                    book = book,
                    onClick = { onAction(BookListAction.OnBookClick(book)) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationsContent(
    state: BookListState,
    onAction: (BookListAction) -> Unit,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isLoadingRecommendations,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LoadingState("Finding perfect matches...")
        }

        AnimatedVisibility(
            visible = !state.isLoadingRecommendations,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            when {
                state.recommendationError != null -> {
                    EmptyState(
                        emoji = "⚠️",
                        title = "Something Went Wrong",
                        message = state.recommendationError.asString(),
                        accentColor = AccentCoral
                    )
                }
                state.favoriteBooks.isEmpty() -> {
                    EmptyState(
                        emoji = "✨",
                        title = "Build Your Profile",
                        message = "Add books to favorites and we'll recommend similar ones just for you",
                        accentColor = AccentMint
                    )
                }
                state.recommendedBooks.isEmpty() -> {
                    EmptyState(
                        emoji = "🎯",
                        title = "No Recommendations Yet",
                        message = "Add more favorites to get better recommendations",
                        accentColor = AccentMint
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        itemsIndexed(
                            items = state.recommendedBooks,
                            key = { index, book -> "${book.id}_rec_$index" }
                        ) { _, book ->
                            BookGridItem(
                                book = book,
                                onClick = { onAction(BookListAction.OnBookClick(book)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentCoral.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = AccentCoral,
                strokeWidth = 3.dp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun EmptyState(
    emoji: String,
    title: String,
    message: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}