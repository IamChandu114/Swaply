package com.swaply.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.swaply.app.data.local.dao.FavoriteDao
import com.swaply.app.data.local.dao.SearchDao
import com.swaply.app.data.local.entity.FavoriteEntity
import com.swaply.app.data.local.entity.RecentSearchEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomDaoTest {

    private lateinit var database: SwaplyDatabase
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var searchDao: SearchDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, SwaplyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        favoriteDao = database.favoriteDao()
        searchDao = database.searchDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndQueryFavorites() = runBlocking {
        favoriteDao.insertFavorite(FavoriteEntity(productId = 101L))
        var isFav = favoriteDao.isFavorite(101L).first()
        assertTrue(isFav)

        favoriteDao.deleteFavorite(101L)
        isFav = favoriteDao.isFavorite(101L).first()
        assertFalse(isFav)
    }

    @Test
    fun insertAndQueryRecentSearches() = runBlocking {
        searchDao.insertSearch(RecentSearchEntity(query = "camera"))
        searchDao.insertSearch(RecentSearchEntity(query = "laptop"))

        val searches = searchDao.getRecentSearches().first()
        assertEquals(2, searches.size)
        assertTrue(searches.any { it.query == "camera" })

        searchDao.deleteSearch("camera")
        val updatedSearches = searchDao.getRecentSearches().first()
        assertEquals(1, updatedSearches.size)
        assertEquals("laptop", updatedSearches[0].query)
    }
}
