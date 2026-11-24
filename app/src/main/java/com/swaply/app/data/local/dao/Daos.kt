package com.swaply.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swaply.app.data.local.entity.FavoriteEntity
import com.swaply.app.data.local.entity.ProductDraftEntity
import com.swaply.app.data.local.entity.ProductEntity
import com.swaply.app.data.local.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id ASC")
    fun pagingSourceByCategory(category: String): PagingSource<Int, ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductFlow(id: Long): Flow<ProductEntity?>
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE productId = :productId)")
    fun isFavorite(productId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE productId = :productId")
    suspend fun deleteFavorite(productId: Long)
}

@Dao
interface SearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()
}

@Dao
interface DraftDao {
    @Query("SELECT * FROM product_drafts WHERE id = 1 LIMIT 1")
    fun getDraft(): Flow<ProductDraftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ProductDraftEntity)

    @Query("DELETE FROM product_drafts WHERE id = 1")
    suspend fun clearDraft()
}
