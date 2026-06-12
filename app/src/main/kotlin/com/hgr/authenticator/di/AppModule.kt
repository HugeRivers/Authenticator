package com.hgr.authenticator.di

import android.content.Context
import androidx.room.Room
import com.hgr.authenticator.data.local.AccountDao
import com.hgr.authenticator.data.local.AuthenticatorDatabase
import com.hgr.authenticator.data.local.PreferencesDataStore
import com.hgr.authenticator.data.repository.AccountRepositoryImpl
import com.hgr.authenticator.data.repository.SettingsRepositoryImpl
import com.hgr.authenticator.domain.repository.AccountRepository
import com.hgr.authenticator.domain.repository.SettingsRepository
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
    fun provideDatabase(@ApplicationContext context: Context): AuthenticatorDatabase =
        Room.databaseBuilder(
            context,
            AuthenticatorDatabase::class.java,
            "authenticator.db"
        ).build()

    @Provides
    fun provideAccountDao(database: AuthenticatorDatabase): AccountDao =
        database.accountDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): PreferencesDataStore =
        PreferencesDataStore(context)

    @Provides
    @Singleton
    fun provideAccountRepository(impl: AccountRepositoryImpl): AccountRepository = impl

    @Provides
    @Singleton
    fun provideSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository = impl
}
