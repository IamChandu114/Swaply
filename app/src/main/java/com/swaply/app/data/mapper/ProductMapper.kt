package com.swaply.app.data.mapper

import com.swaply.app.data.local.entity.ProductDraftEntity
import com.swaply.app.data.local.entity.ProductEntity
import com.swaply.app.data.remote.dto.ProductDto
import com.swaply.app.domain.model.DraftProduct
import com.swaply.app.domain.model.Product
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun ProductDto.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        description = description,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        category = category,
        thumbnail = thumbnail,
        imagesJson = Json.encodeToString(images)
    )
}

fun ProductDto.toDomain(isFavorite: Boolean = false): Product {
    return Product(
        id = id,
        title = title,
        description = description,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        category = category,
        thumbnail = thumbnail,
        images = images,
        isFavorite = isFavorite
    )
}

fun ProductEntity.toDomain(isFavorite: Boolean = false): Product {
    val images = try {
        Json.decodeFromString<List<String>>(imagesJson)
    } catch (e: Exception) {
        listOf(thumbnail)
    }
    return Product(
        id = id,
        title = title,
        description = description,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock,
        brand = brand,
        category = category,
        thumbnail = thumbnail,
        images = images,
        isFavorite = isFavorite
    )
}

fun ProductDraftEntity.toDomain(): DraftProduct {
    val uris = try {
        Json.decodeFromString<List<String>>(imageUrisJson)
    } catch (e: Exception) {
        emptyList()
    }
    return DraftProduct(
        id = id,
        title = title,
        price = price,
        category = category,
        description = description,
        imageUris = uris,
        updatedAt = updatedAt
    )
}

fun DraftProduct.toEntity(): ProductDraftEntity {
    return ProductDraftEntity(
        id = 1L,
        title = title,
        price = price,
        category = category,
        description = description,
        imageUrisJson = Json.encodeToString(imageUris),
        updatedAt = updatedAt
    )
}
