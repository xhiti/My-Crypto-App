package com.baruckis.kriptofolio.dependencyinjection

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.baruckis.kriptofolio.App
import com.baruckis.kriptofolio.BuildConfig
import com.baruckis.kriptofolio.api.ApiService
import com.baruckis.kriptofolio.api.AuthenticationInterceptor
import com.baruckis.kriptofolio.db.AppDatabase
import com.baruckis.kriptofolio.db.CryptocurrencyDao
import com.baruckis.kriptofolio.db.MyCryptocurrencyDao
import com.baruckis.kriptofolio.utilities.API_SERVICE_BASE_URL
import com.baruckis.kriptofolio.utilities.DATABASE_NAME
import com.baruckis.kriptofolio.utilities.LiveDataCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelsModule::class])
class AppModule() {

    @Provides
    @Singleton
    fun provideContext(app: App): Context = app

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(AuthenticationInterceptor())

        builder.retryOnConnectionFailure(false)

        builder.interceptors().add(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideApiService(httpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
                .baseUrl(API_SERVICE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(httpClient)
                .build().create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDb(app: App): AppDatabase {
        return Room
                .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    @Provides
    @Singleton
    fun provideMyCryptocurrencyDao(db: AppDatabase): MyCryptocurrencyDao {
        return db.myCryptocurrencyDao()
    }

    @Provides
    @Singleton
    fun provideCryptocurrencyDao(db: AppDatabase): CryptocurrencyDao {
        return db.cryptocurrencyDao()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(app: App): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
}