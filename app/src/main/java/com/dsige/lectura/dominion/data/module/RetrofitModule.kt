package com.dsige.lectura.dominion.data.module

import com.dsige.lectura.dominion.data.local.AppDataBase
import com.dsige.lectura.dominion.data.local.repository.*
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class RetrofitModule {

    @Provides
    internal fun providesRetrofit(
        gsonFactory: GsonConverterFactory,
        rxJava: RxJava2CallAdapterFactory,
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .addCallAdapterFactory(rxJava)
            .addConverterFactory(gsonFactory)
            .client(client)
            .build()
    }

    @Provides
    internal fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()
    }

    @Provides
    internal fun providesGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().setLenient().create())
    }

    @Provides
    internal fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

    @Provides
    internal fun provideService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    internal fun provideRepository(apiService: ApiService, database: AppDataBase): AppRepository {
        return AppRepoImp(apiService, database)
    }

    @Provides
    internal fun provideError(retrofit: Retrofit): ApiError {
        return ApiError(retrofit)
    }

    companion object {
//        private val BASE_URL = "http://192.168.0.147/webApiDominion/api/Migration/"
        private const val BASE_URL = "http://dominion-peru.com/webApiDominionLecturas/api/Migration/"
    }
}