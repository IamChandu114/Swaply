package com.swaply.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.swaply.app.data.local.dao.DraftDao
import com.swaply.app.data.local.dao.FavoriteDao
import com.swaply.app.data.local.dao.ProductDao
import com.swaply.app.data.local.dao.SearchDao
import com.swaply.app.data.local.entity.FavoriteEntity
import com.swaply.app.data.local.entity.ProductDraftEntity
import com.swaply.app.data.local.entity.ProductEntity
import com.swaply.app.data.local.entity.RecentSearchEntity

@Database(
    entities = [
        ProductEntity::class,
        FavoriteEntity::class,
        RecentSearchEntity::class,
        ProductDraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SwaplyDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchDao(): SearchDao
    abstract fun draftDao(): DraftDao
}
