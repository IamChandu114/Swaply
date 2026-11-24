package com.swaply.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val category: String,
    val thumbnail: String,
    val imagesJson: String
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_drafts")
data class ProductDraftEntity(
    @PrimaryKey val id: Long = 1L,
    val title: String,
    val price: Double,
    val category: String,
    val description: String,
    val imageUrisJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)
