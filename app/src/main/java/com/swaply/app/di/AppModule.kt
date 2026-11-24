package com.swaply.app.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.swaply.app.BuildConfig
import com.swaply.app.data.local.SwaplyDatabase
import com.swaply.app.data.remote.SwaplyApiService
import com.swaply.app.data.repository.ProductRepositoryImpl
import com.swaply.app.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://dummyjson.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideSwaplyApiService(okHttpClient: OkHttpClient, json: Json): SwaplyApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(SwaplyApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSwaplyDatabase(@ApplicationContext context: Context): SwaplyDatabase {
        return Room.databaseBuilder(
            context,
            SwaplyDatabase::class.java,
            "swaply_market.db"
        ).fallbackToDestructiveMigration().build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository
}
