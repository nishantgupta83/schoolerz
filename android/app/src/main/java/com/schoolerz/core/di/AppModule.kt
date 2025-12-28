package com.schoolerz.core.di

import android.content.Context
import com.schoolerz.core.AppMode
import com.schoolerz.core.AppModeHolder
import com.schoolerz.core.util.SecureStorage
import com.schoolerz.data.mock.MockAuthService
import com.schoolerz.data.mock.MockCommentsRepository
import com.schoolerz.data.mock.MockPostRepository
import com.schoolerz.data.mock.MockProfileRepository
import com.schoolerz.data.local.LocalProfileRepository
import com.schoolerz.domain.repository.AuthService
import com.schoolerz.domain.repository.CommentsRepository
import com.schoolerz.domain.repository.PostRepository
import com.schoolerz.domain.repository.ProfileRepository
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
    fun provideAuthService(): AuthService {
        return when (AppModeHolder.current) {
            AppMode.MOCK -> MockAuthService()
            AppMode.FIREBASE -> MockAuthService() // TODO: FirebaseAuthService
        }
    }

    @Provides
    @Singleton
    fun providePostRepository(): PostRepository {
        // Repository selection based on AppMode
        // In mock mode: always use mock repositories
        // In firebase mode: use Firebase repositories (when implemented)
        return when (AppModeHolder.current) {
            AppMode.MOCK -> MockPostRepository()
            AppMode.FIREBASE -> MockPostRepository() // TODO: FirebasePostRepository
        }
    }

    @Provides
    @Singleton
    fun provideCommentsRepository(): CommentsRepository {
        return when (AppModeHolder.current) {
            AppMode.MOCK -> MockCommentsRepository()
            AppMode.FIREBASE -> MockCommentsRepository() // TODO: FirebaseCommentsRepository
        }
    }

    @Provides
    @Singleton
    fun provideProfileRepository(@ApplicationContext context: Context): ProfileRepository {
        // Always use local persistence for profile (syncs with Firebase later)
        return LocalProfileRepository(context)
    }
}
