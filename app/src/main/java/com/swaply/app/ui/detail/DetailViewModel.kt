package com.swaply.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.Product
import com.swaply.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val productId: Long = checkNotNull(savedStateHandle["productId"])

    val productFlow: StateFlow<Product?> = repository.getProductById(productId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _uiState = MutableStateFlow(DetailUiState(isLoading = true))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.refreshProductDetail(productId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        product = result.data,
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }
}
