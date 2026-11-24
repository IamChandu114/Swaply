package com.swaply.app.data.mapper

import com.swaply.app.data.local.entity.ProductDraftEntity
import com.swaply.app.data.local.entity.ProductEntity
import com.swaply.app.data.remote.dto.ProductDto
import com.swaply.app.domain.model.DraftProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductMapperTest {

    @Test
    fun `ProductDto toEntity maps fields correctly`() {
        val dto = ProductDto(
            id = 101L,
            title = "Vintage Watch",
            description = "A classic luxury watch",
            price = 250.0,
            discountPercentage = 5.0,
            rating = 4.8,
            stock = 12,
            brand = "Omega",
            category = "watches",
            thumbnail = "https://thumb.url",
            images = listOf("https://img1.url", "https://img2.url")
        )

        val entity = dto.toEntity()

        assertEquals(101L, entity.id)
        assertEquals("Vintage Watch", entity.title)
        assertEquals(250.0, entity.price, 0.001)
        assertEquals("watches", entity.category)
        assertTrue(entity.imagesJson.contains("https://img1.url"))
    }

    @Test
    fun `ProductEntity toDomain unpacks json images`() {
        val entity = ProductEntity(
            id = 102L,
            title = "Desk Lamp",
            description = "LED Desk Lamp",
            price = 39.99,
            discountPercentage = 0.0,
            rating = 4.5,
            stock = 20,
            brand = "Ikea",
            category = "home",
            thumbnail = "https://lamp.thumb",
            imagesJson = "[\"https://lamp.img1\",\"https://lamp.img2\"]"
        )

        val domain = entity.toDomain(isFavorite = true)

        assertEquals(102L, domain.id)
        assertEquals(2, domain.images.size)
        assertTrue(domain.isFavorite)
    }

    @Test
    fun `DraftProduct toEntity and toDomain round-trip preserves data`() {
        val draft = DraftProduct(
            id = 1L,
            title = "Mechanical Keyboard",
            price = 120.0,
            category = "electronics",
            description = "Custom mechanical keyboard with lubed switches",
            imageUris = listOf("content://media/1", "content://media/2")
        )

        val entity = draft.toEntity()
        val mappedBack = entity.toDomain()

        assertEquals(draft.title, mappedBack.title)
        assertEquals(draft.price, mappedBack.price, 0.001)
        assertEquals(draft.category, mappedBack.category)
        assertEquals(draft.imageUris, mappedBack.imageUris)
    }
}
