package com.example.interviewprep.di

import com.example.interviewprep.data.repository.UserRepositoryImpl
import com.example.interviewprep.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Repository Module
 * 
 * Uses @Binds for interface-implementation binding
 * More efficient than @Provides for simple bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind UserRepository implementation
     * 
     * @Binds tells Hilt:
     * "When someone needs UserRepository, provide UserRepositoryImpl"
     * 
     * Must be abstract function in abstract class
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
