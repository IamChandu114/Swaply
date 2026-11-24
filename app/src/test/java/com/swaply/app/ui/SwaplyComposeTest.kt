package com.swaply.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.swaply.app.domain.model.Product
import com.swaply.app.ui.components.ProductCard
import com.swaply.app.ui.theme.SwaplyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SwaplyComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun productCard_displays_price_and_title() {
        val product = Product(
            id = 42L,
            title = "Sony Wireless Headphones",
            description = "Noise cancelling",
            price = 199.99,
            discountPercentage = 0.0,
            rating = 4.7,
            stock = 5,
            brand = "Sony",
            category = "audio",
            thumbnail = "https://thumb.url",
            images = listOf("https://img.url"),
            isFavorite = false
        )

        composeTestRule.setContent {
            SwaplyTheme {
                ProductCard(
                    product = product,
                    onProductClick = {},
                    onToggleFavorite = {}
                )
            }
        }

        composeTestRule.onNodeWithText("$199.99").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sony Wireless Headphones").assertIsDisplayed()
    }

    @Test
    fun productCard_toggle_favorite_invokes_callback() {
        var clickedId = 0L
        val product = Product(
            id = 88L,
            title = "Mechanical Keyboard",
            description = "RGB mechanical keyboard",
            price = 89.0,
            discountPercentage = 0.0,
            rating = 4.6,
            stock = 15,
            brand = "Keychron",
            category = "electronics",
            thumbnail = "https://thumb.url",
            images = emptyList(),
            isFavorite = false
        )

        composeTestRule.setContent {
            SwaplyTheme {
                ProductCard(
                    product = product,
                    onProductClick = {},
                    onToggleFavorite = { clickedId = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("favorite_button_88").performClick()
        assertEquals(88L, clickedId)
    }
}
