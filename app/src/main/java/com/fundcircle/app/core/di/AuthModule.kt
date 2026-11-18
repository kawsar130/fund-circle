package com.fundcircle.app.core.di

import com.fundcircle.app.data.datasource.local.SessionLocalDataSource
import com.fundcircle.app.data.datasource.local.SessionLocalDataSourceImpl
import com.fundcircle.app.data.datasource.remote.AuthRemoteDataSource
import com.fundcircle.app.data.datasource.remote.AuthRemoteDataSourceImpl
import com.fundcircle.app.data.repository.AuthRepositoryImpl
import com.fundcircle.app.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindSessionLocalDataSource(
        impl: SessionLocalDataSourceImpl
    ): SessionLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(
        impl: AuthRemoteDataSourceImpl
    ): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
