package com.swaply.app.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.swaply.app.core.util.Resource
import com.swaply.app.data.local.SwaplyDatabase
import com.swaply.app.data.local.entity.FavoriteEntity
import com.swaply.app.data.local.entity.RecentSearchEntity
import com.swaply.app.data.mapper.toDomain
import com.swaply.app.data.mapper.toEntity
import com.swaply.app.data.paging.ProductRemoteMediator
import com.swaply.app.data.remote.SwaplyApiService
import com.swaply.app.data.remote.dto.AddProductRequest
import com.swaply.app.domain.model.DraftProduct
import com.swaply.app.domain.model.Product
import com.swaply.app.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: SwaplyApiService,
    private val database: SwaplyDatabase
) : ProductRepository {

    private val productDao = database.productDao()
    private val favoriteDao = database.favoriteDao()
    private val searchDao = database.searchDao()
    private val draftDao = database.draftDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getProductFeed(category: String?): Flow<PagingData<Product>> {
        val pagingSourceFactory = {
            if (category.isNullOrEmpty() || category == "All") {
                productDao.pagingSource()
            } else {
                productDao.pagingSourceByCategory(category)
            }
        }

        val favoriteIdsFlow = favoriteDao.getAllFavorites()
            .map { list -> list.map { it.productId }.toSet() }

        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false, prefetchDistance = 5),
            remoteMediator = ProductRemoteMediator(apiService, database, category),
            pagingSourceFactory = pagingSourceFactory
        ).flow.combine(favoriteIdsFlow) { pagingData, favIds ->
            pagingData.map { entity ->
                entity.toDomain(isFavorite = favIds.contains(entity.id))
            }
        }
    }

    override fun getProductById(id: Long): Flow<Product?> {
        return productDao.getProductFlow(id).combine(favoriteDao.isFavorite(id)) { entity, isFav ->
            entity?.toDomain(isFavorite = isFav)
        }
    }

    override suspend fun refreshProductDetail(id: Long): Resource<Product> {
        return try {
            val dto = apiService.getProductDetail(id)
            val isFav = favoriteDao.isFavorite(id).first()
            productDao.insertAll(listOf(dto.toEntity()))
            Resource.Success(dto.toDomain(isFavorite = isFav))
        } catch (e: Exception) {
            val cached = productDao.getProductById(id)
            if (cached != null) {
                val isFav = favoriteDao.isFavorite(id).first()
                Resource.Success(cached.toDomain(isFavorite = isFav))
            } else {
                Resource.Error(e.localizedMessage ?: "Failed to load product details", e)
            }
        }
    }

    override fun searchProducts(query: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.searchProducts(query)
            val favIds = favoriteDao.getAllFavorites().first().map { it.productId }.toSet()
            val products = response.products.map { it.toDomain(isFavorite = favIds.contains(it.id)) }
            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Search failed. Please check connection.", e))
        }
    }

    override fun getRecentSearches(): Flow<List<String>> {
        return searchDao.getRecentSearches().map { list -> list.map { it.query } }
    }

    override suspend fun saveRecentSearch(query: String) {
        if (query.isNotBlank()) {
            searchDao.insertSearch(RecentSearchEntity(query = query.trim()))
        }
    }

    override suspend fun removeRecentSearch(query: String) {
        searchDao.deleteSearch(query)
    }

    override suspend fun clearRecentSearches() {
        searchDao.clearRecentSearches()
    }

    override fun getFavoriteIds(): Flow<Set<Long>> {
        return favoriteDao.getAllFavorites().map { list -> list.map { it.productId }.toSet() }
    }

    override suspend fun toggleFavorite(productId: Long) {
        val isFav = favoriteDao.isFavorite(productId).first()
        if (isFav) {
            favoriteDao.deleteFavorite(productId)
        } else {
            favoriteDao.insertFavorite(FavoriteEntity(productId = productId))
        }
    }

    override fun getDraft(): Flow<DraftProduct?> {
        return draftDao.getDraft().map { it?.toDomain() }
    }

    override suspend fun saveDraft(draft: DraftProduct) {
        draftDao.saveDraft(draft.toEntity())
    }

    override suspend fun clearDraft() {
        draftDao.clearDraft()
    }

    override suspend fun publishProduct(draft: DraftProduct): Resource<Long> {
        return try {
            val response = apiService.addProduct(
                AddProductRequest(
                    title = draft.title,
                    price = draft.price,
                    description = draft.description,
                    category = draft.category
                )
            )
            clearDraft()
            Resource.Success(response.id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to publish listing", e)
        }
    }

    override suspend fun getCategories(): Resource<List<String>> {
        return try {
            val categories = apiService.getCategories()
            Resource.Success(listOf("All") + categories)
        } catch (e: Exception) {
            Resource.Success(listOf("All", "smartphones", "laptops", "fragrances", "skincare", "groceries", "home-decoration"))
        }
    }
}
