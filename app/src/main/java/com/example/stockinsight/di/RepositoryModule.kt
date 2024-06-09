package com.example.stockinsight.di

import android.content.Context
import com.example.stockinsight.data.repository.AuthenticationImpl
import com.example.stockinsight.data.repository.AuthenticationRepository
import com.example.stockinsight.data.repository.StockImpl
import com.example.stockinsight.data.repository.StockRepository
import com.example.stockinsight.data.repository.UserImpl
import com.example.stockinsight.data.repository.UserRepository
import com.example.stockinsight.data.socket.SocketManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        @ApplicationContext context: Context, auth: FirebaseAuth, database: FirebaseFirestore
    ): AuthenticationRepository {
        return AuthenticationImpl(context, auth, database)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        @ApplicationContext context: Context, database: FirebaseFirestore, auth: FirebaseAuth
    ): UserRepository {
        return UserImpl(context, database, auth)
    }

    @Provides
    @Singleton
    fun provideStockRepository(
        @ApplicationContext context: Context, database: FirebaseFirestore, socketManager: SocketManager
    ): StockRepository {
        return StockImpl(context, database, socketManager)
    }
}