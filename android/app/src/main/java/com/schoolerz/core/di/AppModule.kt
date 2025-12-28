package com.schoolerz.core.di

import android.content.Context
import com.schoolerz.core.util.SecureStorage
import com.schoolerz.data.mock.MockCommentsRepository
import com.schoolerz.data.mock.MockPostRepository
import com.schoolerz.domain.repository.CommentsRepository
import com.schoolerz.domain.repository.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage =
        SecureStorage(context)

    @Provides
    @Singleton
    fun providePostRepository(): PostRepository = MockPostRepository()

    @Provides
    @Singleton
    fun provideCommentsRepository(): CommentsRepository = MockCommentsRepository()
}
