package com.example.stockinsight.di

import com.example.stockinsight.data.remote.YFinanceApi
import com.example.stockinsight.data.remote.YahooFinanceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.example.stockinsight.utils.Constants
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    @Provides
    @Singleton
    @Named("RapidApiYahooFinance")
    fun provideRetrofitRapidApiYahooFinance(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .addHeader(
                        Constants.RAPID_API_KEY,
                        Constants.RAPID_API_KEY_VALUE
                    )
                    .addHeader(Constants.RAPID_API_HOST, Constants.RAPID_API_HOST_VALUE)
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_RAPID_API)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("YFinance")
    fun provideRetrofitYFinance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_YFINANCE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideYahooFinanceApi(@Named("RapidApiYahooFinance") retrofit: Retrofit): YahooFinanceApi {
        return retrofit.create(YahooFinanceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYFinanceApi(@Named("YFinance") retrofit: Retrofit): YFinanceApi {
        return retrofit.create(YFinanceApi::class.java)
    }
}