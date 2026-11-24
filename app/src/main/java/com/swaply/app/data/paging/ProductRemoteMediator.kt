package com.swaply.app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.swaply.app.data.local.SwaplyDatabase
import com.swaply.app.data.local.entity.ProductEntity
import com.swaply.app.data.mapper.toEntity
import com.swaply.app.data.remote.SwaplyApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    private val apiService: SwaplyApiService,
    private val database: SwaplyDatabase,
    private val category: String? = null
) : RemoteMediator<Int, ProductEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ProductEntity>
    ): MediatorResult {
        return try {
            val skip = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val count = state.pages.sumOf { it.data.size }
                    if (count == 0) return MediatorResult.Success(endOfPaginationReached = false)
                    count
                }
            }

            val limit = state.config.pageSize
            val response = if (category.isNullOrEmpty() || category == "All") {
                apiService.getProducts(limit = limit, skip = skip)
            } else {
                apiService.getProductsByCategory(category = category, limit = limit, skip = skip)
            }

            val endOfPaginationReached = response.products.isEmpty() || (skip + response.products.size >= response.total)

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    if (category.isNullOrEmpty() || category == "All") {
                        database.productDao().clearAll()
                    }
                }
                val entities = response.products.map { it.toEntity() }
                database.productDao().insertAll(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
