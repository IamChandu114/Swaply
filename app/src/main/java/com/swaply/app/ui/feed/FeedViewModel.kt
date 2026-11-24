package com.swaply.app.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.swaply.app.core.network.NetworkMonitor
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.Product
import com.swaply.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val categories: List<String> = listOf("All"),
    val selectedCategory: String = "All",
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: ProductRepository,
    networkMonitor: NetworkMonitor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryKey = "selected_category"
    private val _selectedCategory = MutableStateFlow(savedStateHandle.get<String>(categoryKey) ?: "All")

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    val products: Flow<PagingData<Product>> = _selectedCategory.flatMapLatest { cat ->
        repository.getProductFeed(if (cat == "All") null else cat)
    }.cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val res = repository.getCategories()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(categories = res.data)
                }
                else -> Unit
            }
        }
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        savedStateHandle[categoryKey] = category
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onToggleFavorite(productId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }
}
