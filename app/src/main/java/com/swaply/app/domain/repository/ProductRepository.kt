package com.swaply.app.domain.repository

import androidx.paging.PagingData
import com.swaply.app.core.util.Resource
import com.swaply.app.domain.model.DraftProduct
import com.swaply.app.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProductFeed(category: String? = null): Flow<PagingData<Product>>
    fun getProductById(id: Long): Flow<Product?>
    suspend fun refreshProductDetail(id: Long): Resource<Product>
    fun searchProducts(query: String): Flow<Resource<List<Product>>>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveRecentSearch(query: String)
    suspend fun removeRecentSearch(query: String)
    suspend fun clearRecentSearches()
    fun getFavoriteIds(): Flow<Set<Long>>
    suspend fun toggleFavorite(productId: Long)
    fun getDraft(): Flow<DraftProduct?>
    suspend fun saveDraft(draft: DraftProduct)
    suspend fun clearDraft()
    suspend fun publishProduct(draft: DraftProduct): Resource<Long>
    suspend fun getCategories(): Resource<List<String>>
}
