package com.example.stockinsight.di

import com.example.stockinsight.data.remote.PostApi
import com.example.stockinsight.data.repository.AuthenticationImpl
import com.example.stockinsight.data.repository.AuthenticationRepository
import com.example.stockinsight.data.repository.PostImpl
import com.example.stockinsight.data.repository.PostRepository
import com.example.stockinsight.data.repository.StockImpl
import com.example.stockinsight.data.repository.StockRepository
import com.example.stockinsight.data.repository.UserImpl
import com.example.stockinsight.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        auth: FirebaseAuth,
        database: FirebaseFirestore
    ): AuthenticationRepository {
        return AuthenticationImpl(auth, database)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        database: FirebaseFirestore
    ): UserRepository {
        return UserImpl(database)
    }

    @Provides
    @Singleton
    fun provideStockRepository(): StockRepository {
        return StockImpl()
    }

    @Provides
    @Singleton
    fun providePostRepository(
        postApi: PostApi
    ): PostRepository {
        return PostImpl(postApi)
    }
}