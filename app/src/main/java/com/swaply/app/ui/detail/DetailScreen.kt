package com.swaply.app.ui.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val localProduct by viewModel.productFlow.collectAsState()
    val product = localProduct ?: uiState.product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.title ?: "Product Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (product != null) {
                        IconButton(
                            onClick = viewModel::onToggleFavorite,
                            modifier = Modifier.testTag("detail_favorite_toggle")
                        ) {
                            Icon(
                                imageVector = if (product.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (product.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (product.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading && product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (product != null) {
            val images = if (product.images.isNotEmpty()) product.images else listOf(product.thumbnail)
            val pagerState = rememberPagerState(pageCount = { images.size })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = images[page],
                            contentDescription = "Image ${page + 1} of ${product.title}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                        ) {
                            repeat(images.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${product.rating} / 5.0",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        Text(
                            text = "Category: ${product.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Seller details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verified Marketplace Seller (Brand: ${product.brand ?: "Individual"})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
