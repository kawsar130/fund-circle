package com.fundcircle.app.core.di

import com.fundcircle.app.data.datasource.remote.FundRemoteDataSource
import com.fundcircle.app.data.datasource.remote.FundRemoteDataSourceImpl
import com.fundcircle.app.data.repository.FundRepositoryImpl
import com.fundcircle.app.domain.repository.FundRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FundModule {

    @Binds
    @Singleton
    abstract fun bindFundRemoteDataSource(
        impl: FundRemoteDataSourceImpl
    ): FundRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindFundRepository(
        impl: FundRepositoryImpl
    ): FundRepository
}
