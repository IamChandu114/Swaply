package com.swaply.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.swaply.app.ui.components.OfflineBanner
import com.swaply.app.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pagingItems = viewModel.products.collectAsLazyPagingItems()

    val pullToRefreshState = rememberPullToRefreshState()
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            pagingItems.refresh()
            pullToRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swaply Marketplace") },
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search products")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OfflineBanner(isOffline = !isOnline)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                if (pagingItems.loadState.refresh is LoadState.Loading && pagingItems.itemCount == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("feed_loading_indicator"))
                    }
                } else if (pagingItems.loadState.refresh is LoadState.Error && pagingItems.itemCount == 0) {
                    val error = (pagingItems.loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error.localizedMessage ?: "Failed to load feed",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { pagingItems.retry() }) {
                            Text("Retry")
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("feed_grid")
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = pagingItems.itemKey { it.id }
                        ) { index ->
                            val product = pagingItems[index]
                            if (product != null) {
                                ProductCard(
                                    product = product,
                                    onProductClick = onNavigateToDetail,
                                    onToggleFavorite = { viewModel.onToggleFavorite(it) }
                                )
                            }
                        }

                        if (pagingItems.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }

                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
