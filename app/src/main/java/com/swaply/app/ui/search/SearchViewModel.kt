package com.swaply.app.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.Product
import com.swaply.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Product> = emptyList(),
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val queryKey = "search_query"
    private val _query = MutableStateFlow(savedStateHandle.get<String>(queryKey) ?: "")
    val query = _query.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<String>> = repository.getRecentSearches().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            _query
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { q ->
                    if (q.isBlank()) {
                        flowOf(Resource.Success(emptyList()))
                    } else {
                        repository.saveRecentSearch(q)
                        repository.searchProducts(q)
                    }
                }
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                        }
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                results = resource.data,
                                errorMessage = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        savedStateHandle[queryKey] = newQuery
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun onClearQuery() {
        onQueryChange("")
    }

    fun onSelectRecent(recent: String) {
        onQueryChange(recent)
    }

    fun onRemoveRecent(recent: String) {
        viewModelScope.launch {
            repository.removeRecentSearch(recent)
        }
    }

    fun onClearAllRecent() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    fun onToggleFavorite(productId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }
}
