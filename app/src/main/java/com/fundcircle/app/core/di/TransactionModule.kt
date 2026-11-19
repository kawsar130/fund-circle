package com.fundcircle.app.core.di

import com.fundcircle.app.data.datasource.remote.TransactionRemoteDataSource
import com.fundcircle.app.data.datasource.remote.TransactionRemoteDataSourceImpl
import com.fundcircle.app.data.repository.TransactionRepositoryImpl
import com.fundcircle.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRemoteDataSource(
        impl: TransactionRemoteDataSourceImpl
    ): TransactionRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
}
