package com.sumit.simplemobileaisuite.di

import com.sumit.simplemobileaisuite.core.permission.PermissionManager
import com.sumit.simplemobileaisuite.core.permission.PermissionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module to provide dependencies related to permissions.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionModule {

    @Binds
    @Singleton
    abstract fun bindPermissionManager(
        permissionManagerImpl: PermissionManagerImpl
    ): PermissionManager
}
