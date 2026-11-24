package com.swaply.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double = 0.0,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val brand: String? = null,
    val category: String,
    val thumbnail: String,
    val images: List<String> = emptyList()
)

@Serializable
data class ProductResponseDto(
    val products: List<ProductDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

@Serializable
data class AddProductRequest(
    val title: String,
    val price: Double,
    val description: String,
    val category: String
)

@Serializable
data class AddProductResponse(
    val id: Long,
    val title: String,
    val price: Double,
    val description: String,
    val category: String
)
