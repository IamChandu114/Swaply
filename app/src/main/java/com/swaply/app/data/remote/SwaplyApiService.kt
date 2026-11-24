package com.swaply.app.data.remote

import com.swaply.app.data.remote.dto.AddProductRequest
import com.swaply.app.data.remote.dto.AddProductResponse
import com.swaply.app.data.remote.dto.ProductDto
import com.swaply.app.data.remote.dto.ProductResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SwaplyApiService {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int
    ): ProductResponseDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): ProductResponseDto

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(
        @Path("category") category: String,
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): ProductResponseDto

    @GET("products/categories")
    suspend fun getCategories(): List<String>

    @GET("products/{id}")
    suspend fun getProductDetail(
        @Path("id") id: Long
    ): ProductDto

    @POST("products/add")
    suspend fun addProduct(
        @Body request: AddProductRequest
    ): AddProductResponse
}
