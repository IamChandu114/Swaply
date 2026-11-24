package com.swaply.app.ui.sell

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.DraftProduct
import com.swaply.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellFormState(
    val title: String = "",
    val price: String = "",
    val category: String = "smartphones",
    val description: String = "",
    val imageUris: List<String> = emptyList(),
    val titleError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val isSubmitting: Boolean = false
)

sealed interface SellUiEvent {
    data class PublishSuccess(val productId: Long) : SellUiEvent
    data class PublishError(val message: String) : SellUiEvent
}

@HiltViewModel
class SellViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _formState = MutableStateFlow(SellFormState())
    val formState: StateFlow<SellFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<SellUiEvent>()
    val events: SharedFlow<SellUiEvent> = _events.asSharedFlow()

    init {
        restoreDraft()
    }

    private fun restoreDraft() {
        viewModelScope.launch {
            val draft = repository.getDraft().firstOrNull()
            if (draft != null) {
                _formState.value = _formState.value.copy(
                    title = draft.title,
                    price = if (draft.price > 0.0) draft.price.toString() else "",
                    category = draft.category.ifBlank { "smartphones" },
                    description = draft.description,
                    imageUris = draft.imageUris
                )
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _formState.value = _formState.value.copy(
            title = newTitle,
            titleError = if (newTitle.trim().length < 3) "Title must be at least 3 characters" else null
        )
        persistDraft()
    }

    fun onPriceChange(newPrice: String) {
        val parsed = newPrice.toDoubleOrNull()
        _formState.value = _formState.value.copy(
            price = newPrice,
            priceError = if (parsed == null || parsed <= 0.0) "Enter a valid price > 0" else null
        )
        persistDraft()
    }

    fun onCategoryChange(newCategory: String) {
        _formState.value = _formState.value.copy(category = newCategory)
        persistDraft()
    }

    fun onDescriptionChange(newDesc: String) {
        _formState.value = _formState.value.copy(
            description = newDesc,
            descriptionError = if (newDesc.trim().length < 10) "Description must be at least 10 characters" else null
        )
        persistDraft()
    }

    fun onAddPhoto(uriString: String) {
        _formState.value = _formState.value.copy(
            imageUris = _formState.value.imageUris + uriString
        )
        persistDraft()
    }

    fun onRemovePhoto(uriString: String) {
        _formState.value = _formState.value.copy(
            imageUris = _formState.value.imageUris.filter { it != uriString }
        )
        persistDraft()
    }

    private fun persistDraft() {
        viewModelScope.launch {
            val current = _formState.value
            val draft = DraftProduct(
                title = current.title,
                price = current.price.toDoubleOrNull() ?: 0.0,
                category = current.category,
                description = current.description,
                imageUris = current.imageUris
            )
            repository.saveDraft(draft)
        }
    }

    fun submit() {
        val current = _formState.value
        val titleValid = current.title.trim().length >= 3
        val priceValid = (current.price.toDoubleOrNull() ?: 0.0) > 0.0
        val descValid = current.description.trim().length >= 10

        if (!titleValid || !priceValid || !descValid) {
            _formState.value = current.copy(
                titleError = if (!titleValid) "Title must be at least 3 characters" else null,
                priceError = if (!priceValid) "Enter a valid price > 0" else null,
                descriptionError = if (!descValid) "Description must be at least 10 characters" else null
            )
            return
        }

        viewModelScope.launch {
            _formState.value = _formState.value.copy(isSubmitting = true)
            val draft = DraftProduct(
                title = current.title.trim(),
                price = current.price.toDouble(),
                category = current.category,
                description = current.description.trim(),
                imageUris = current.imageUris
            )
            when (val res = repository.publishProduct(draft)) {
                is Resource.Success -> {
                    _formState.value = SellFormState()
                    _events.emit(SellUiEvent.PublishSuccess(res.data))
                }
                is Resource.Error -> {
                    _formState.value = _formState.value.copy(isSubmitting = false)
                    _events.emit(SellUiEvent.PublishError(res.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
