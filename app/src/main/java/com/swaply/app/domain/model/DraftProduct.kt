package com.swaply.app.domain.model

data class DraftProduct(
    val id: Long = 0,
    val title: String,
    val price: Double,
    val category: String,
    val description: String,
    val imageUris: List<String>,
    val updatedAt: Long = System.currentTimeMillis()
)
