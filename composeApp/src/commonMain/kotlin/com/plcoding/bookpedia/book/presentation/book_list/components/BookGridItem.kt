package com.plcoding.bookpedia.book.presentation.book_list.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cmp_bookpedia.composeapp.generated.resources.Res
import cmp_bookpedia.composeapp.generated.resources.book_error_2
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.plcoding.bookpedia.book.domain.Book
import com.plcoding.bookpedia.core.presentation.PulseAnimation
import org.jetbrains.compose.resources.painterResource
import kotlin.math.round

// Elegant color palette
private val AccentCoral = Color(0xFFFF7F7F)
private val AccentMint = Color(0xFF7BDCB5)
private val CardGradientColors = listOf(
    listOf(Color(0xFFFFB6B9), Color(0xFFFF8C94)), // Coral gradient
    listOf(Color(0xFF89D4CF), Color(0xFF6EDCD9)), // Mint gradient
    listOf(Color(0xFFB4A7D6), Color(0xFF9B86BD)), // Lavender gradient
    listOf(Color(0xFFFFDAB9), Color(0xFFFFB88C)), // Peach gradient
    listOf(Color(0xFF87CEEB), Color(0xFF6CB4EE)), // Sky blue gradient
)

@Composable
fun BookGridItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Select gradient based on book ID for consistency
    val gradientIndex = remember(book.id) {
        book.id.hashCode().let { if (it < 0) -it else it } % CardGradientColors.size
    }
    val gradient = CardGradientColors[gradientIndex]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Book Cover Section with Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(gradient)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                BookCoverImage(book = book)
            }

            // Book Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.titleSmall.lineHeight
                )

                Spacer(modifier = Modifier.height(4.dp))

                book.authors.firstOrNull()?.let { author ->
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF718096),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                book.averageRating?.let { rating ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFA500),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${round(rating * 10) / 10.0}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCoverImage(book: Book) {
    var imageLoadResult by remember { mutableStateOf<Result<Painter>?>(null) }

    val painter = rememberAsyncImagePainter(
        model = book.imageUrl,
        onSuccess = {
            imageLoadResult = if (it.painter.intrinsicSize.width > 1 &&
                it.painter.intrinsicSize.height > 1) {
                Result.success(it.painter)
            } else {
                Result.failure(Exception("Invalid image size"))
            }
        },
        onError = {
            it.result.throwable.printStackTrace()
            imageLoadResult = Result.failure(it.result.throwable)
        }
    )

    val painterState by painter.state.collectAsStateWithLifecycle()
    val transition by animateFloatAsState(
        targetValue = if(painterState is AsyncImagePainter.State.Success) 1f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val result = imageLoadResult) {
            null -> {
                PulseAnimation(
                    modifier = Modifier.size(60.dp)
                )
            }
            else -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(0.7f)
                        .graphicsLayer {
                            val scale = 0.85f + (0.15f * transition)
                            scaleX = scale
                            scaleY = scale
                            alpha = transition
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (result.isSuccess) 8.dp else 0.dp
                    )
                ) {
                    Image(
                        painter = if (result.isSuccess) painter else {
                            painterResource(Res.drawable.book_error_2)
                        },
                        contentDescription = book.title,
                        contentScale = if (result.isSuccess) {
                            ContentScale.Crop
                        } else {
                            ContentScale.Fit
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}