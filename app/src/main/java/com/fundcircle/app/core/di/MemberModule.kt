package com.fundcircle.app.core.di

import com.fundcircle.app.data.datasource.remote.MemberRemoteDataSource
import com.fundcircle.app.data.datasource.remote.MemberRemoteDataSourceImpl
import com.fundcircle.app.data.repository.MemberRepositoryImpl
import com.fundcircle.app.domain.repository.MemberRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MemberModule {

    @Binds
    @Singleton
    abstract fun bindMemberRemoteDataSource(
        impl: MemberRemoteDataSourceImpl
    ): MemberRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMemberRepository(
        impl: MemberRepositoryImpl
    ): MemberRepository
}
