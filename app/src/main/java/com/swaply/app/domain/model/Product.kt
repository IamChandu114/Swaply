package com.swaply.app.domain.model

data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val category: String,
    val thumbnail: String,
    val images: List<String>,
    val isFavorite: Boolean = false
)
