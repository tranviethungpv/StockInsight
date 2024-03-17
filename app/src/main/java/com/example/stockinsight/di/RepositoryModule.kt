package com.example.stockinsight.di

import com.example.stockinsight.data.repository.AuthenticationImpl
import com.example.stockinsight.data.repository.AuthenticationRepository
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
}